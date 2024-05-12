package cn.mw.monitor.configmanage.service.impl;

import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import cn.mw.monitor.configmanage.service.Parser;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.configmanage.dao.MwConfigManageTableDao;
import cn.mw.monitor.configmanage.entity.*;
import cn.mw.monitor.configmanage.service.EncryptUtil;
import cn.mw.monitor.configmanage.service.MwPerfromService;
import cn.mw.monitor.templatemanage.entity.QueryTemplateManageParam;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;


@Service
@Slf4j
@Transactional
public class MwPerformService implements Callable<PerformResultEntity> {

    private String cmds;

    private Boolean b;

    private MwTangibleassetsTable param;

    private ILoginCacheInfo iLoginCacheInfo;

    private MwConfigManageTableDao mwTangibleAssetsDao;

    private MwPerfromService mwPerfromService;

    private CountDownLatch latch;

    public MwPerformService() {
    }

    public MwPerformService(CountDownLatch latch , MwTangibleassetsTable param, ILoginCacheInfo iLoginCacheInfo,
                            MwConfigManageTableDao mwTangibleAssetsDao, MwPerfromService mwPerfromService,
                            Boolean b,String cmds) {
        this.param = param;
        this.iLoginCacheInfo = iLoginCacheInfo;
        this.mwTangibleAssetsDao = mwTangibleAssetsDao;
        this.mwPerfromService = mwPerfromService;
        this.latch = latch;
        this.b = b;
        this.cmds = cmds;
    }

    @Override
    public PerformResultEntity call() {
        try {
            MwTangibleassetsTable param = this.param;
            String assetsId = param.getId();
            //1 获得要执行的命令；
            String cmds = this.cmds;
            String[] cmd = cmds.split("\n");

            //TODO
            MwTemplateMapper templateMapper = mwTangibleAssetsDao.selectTemplateMapper(assetsId);
            QueryTemplateManageParam template = mwTangibleAssetsDao.selectOneTemplate(templateMapper.getTemplateId());
            Map<String,String> xmlMaps = new HashMap<>();
            String xml = template.getXml();
            Document doc = DocumentHelper.parseText(xml);
            XPath xPath = DocumentHelper.createXPath("//Command");
            List nodes = xPath.selectNodes(doc);
            for(Object obj : nodes){
                Element n = (Element)obj;
                String key = n.attribute("Name").getText();
                String value = n.attribute("Value").getText();
                xmlMaps.put(key,value);
            }
            //判断是否含有restart字段
            String reset = xmlMaps.get("RESET");
            String[] moreCmds;
//            if(reset!=null && !reset.equals("")){
//                moreCmds = new String[cmd.length+1];
//                for (int i = 0; i < cmd.length; i++) {
//                    moreCmds[i] = cmd[i];
//                }
//                moreCmds[cmd.length] = reset;
//            }else {
                moreCmds = cmd;
//            }

            //2 查询出 账号信息
            MwAccountMapper accountMapper = mwTangibleAssetsDao.selectAccountMapper(assetsId);
            QueryAccountManageParam account = mwTangibleAssetsDao.selectOneAccount(accountMapper.getAccountId());
            if(account == null) {
                PerformResultEntity res = PerformResultEntity.builder().ip(param.getInBandIp())
                        .hostName(param.getHostName()).isSuccess(false).results("该设备关联信息出错,无法执行").build();
                return res;
            }

            //3 使用相关协议执行命令
            String protocol = account.getProtocol();
            if("TELNET".equals(protocol)) {
                Boolean flag = true;
                String result = mwPerfromService.telent2(param.getInBandIp(), Integer.parseInt(account.getPort()),
                        account.getUsername(), EncryptUtil.decrypt(account.getPassword()), moreCmds);
                if(result.equals("login error") || result.equals("Connection timed out")){
                    flag = false;
                }
                //4 保存配置相关信息(配置信息内容太多，单独保存一个txt文件)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String name = param.getHostName()+"_"+sdf.format(new Date())+"_"+((int)(Math.random()*(9000)+1000));

                MwNcmPath mwNcmPath = mwTangibleAssetsDao.getPath();
                String path = mwNcmPath.getPerfromPath();
                String s = savDownloadConfig(name, path, result);

                MwNcmDownloadConfig data = new MwNcmDownloadConfig();
                data.setAssetsId(param.getAssetsId());
                data.setConfigType(cmds);
                data.setName(name + ".mwcfg");
                data.setPath(path);
                data.setCreateDate(new Date());
                if(b == true){
                    data.setCreator(iLoginCacheInfo.getLoginName());
                }
                mwTangibleAssetsDao.saveConfigPerfrom(data);
                String pa = path+"/"+name + ".mwcfg";
                PerformResultEntity res = PerformResultEntity.builder().ip(param.getInBandIp())
                        .hostName(param.getHostName()).isSuccess(flag).path(pa).results(result).build();
                return res;
            }else if("SSH".equals(protocol)){
                Boolean flag = true;
                String result = mwPerfromService.sshDownload(param.getInBandIp(),Integer.parseInt(account.getPort()),
                        account.getUsername(),EncryptUtil.decrypt(account.getPassword()),moreCmds,200);
                if(result.equals("ssh cmds is null") || result.equals("ssh login error")){
                    flag = false;
                }
                //System.err.println(result);
                //4 保存配置相关信息(配置信息内容太多，单独保存一个txt文件)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String name = param.getHostName()+"_"+sdf.format(new Date())+"_"+((int)(Math.random()*(9000)+1000));

                MwNcmPath mwNcmPath = mwTangibleAssetsDao.getPath();
                String path = mwNcmPath.getPerfromPath();
                String s = savDownloadConfig(name,path,result);

                MwNcmDownloadConfig data = new MwNcmDownloadConfig();
                data.setAssetsId(param.getAssetsId());
                data.setConfigType(cmds);
                data.setName(name+".mwcfg");
                data.setPath(path);
                data.setCreateDate(new Date());
                if(b == true){
                    data.setCreator(iLoginCacheInfo.getLoginName());
                }
                mwTangibleAssetsDao.saveConfigPerfrom(data);
                String pa = path+"/"+name + ".mwcfg";
                PerformResultEntity res = PerformResultEntity.builder().ip(param.getInBandIp())
                        .hostName(param.getHostName()).isSuccess(flag).path(pa).results(result).build();
                return res;
            }
        }catch (Exception e){
            log.error("下载配置文件失败",e);
            PerformResultEntity res = PerformResultEntity.builder().ip(param.getInBandIp())
                    .hostName(param.getHostName()).isSuccess(false).results(e.getMessage()).build();
            return res;
        }finally {
            if (latch != null) {
                latch.countDown();
            }
        }
        return null;
    }

    private String savDownloadConfig(String name, String path, String result) {
        FileOutputStream fop = null;
        try {
            //创建目录
            File f = new File(path);
            if(!f.exists()){
                f.mkdirs();
            }

            File file = new File(path,name+".mwcfg");
            fop = new FileOutputStream(file);
            if(!file.exists()){
                file.createNewFile();
            }
            String str = EncryptUtil.encrypt(result);
            byte[] context = str.getBytes();
            //byte[] context = result.getBytes();
            fop.write(context);
            fop.flush();
            fop.close();

        } catch (FileNotFoundException e) {
            log.error("下载配置文件失败",e);
        }catch (IOException e) {
            log.error("下载配置文件失败",e);
        }finally {
            if(fop != null){
                try {
                    fop.close();
                } catch (IOException e) {
                    log.error("下载配置文件失败",e);
                }
            }
        }
        return "保存成功";
    }

    
}
