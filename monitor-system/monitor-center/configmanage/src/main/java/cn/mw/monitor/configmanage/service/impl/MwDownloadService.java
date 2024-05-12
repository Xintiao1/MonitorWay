package cn.mw.monitor.configmanage.service.impl;

import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import cn.mw.monitor.configmanage.dao.MwConfigManageTableDao;
import cn.mw.monitor.configmanage.entity.*;
import cn.mw.monitor.configmanage.service.EncryptUtil;
import cn.mw.monitor.configmanage.service.MwPerfromService;
import cn.mw.monitor.configmanage.service.Parser;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.templatemanage.entity.QueryTemplateManageParam;
import cn.mwpaas.common.utils.StringUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
@Slf4j
@Transactional
public class MwDownloadService   {

    private String configType;

    private Boolean b;

    private MwTangibleassetsTable param;

    private ILoginCacheInfo iLoginCacheInfo;

    private MwConfigManageTableDao mwTangibleAssetsDao;

    private MwPerfromService mwPerfromService;

   private MWCommonService mwCommonService;

    public MwDownloadService() {
    }

    public

    MwDownloadService(MwTangibleassetsTable param,
                             ILoginCacheInfo iLoginCacheInfo, MwConfigManageTableDao mwTangibleAssetsDao,
                             MwPerfromService mwPerfromService,Boolean b,String configType,MWCommonService mwCommonService) {
        this.param = param;
        this.iLoginCacheInfo = iLoginCacheInfo;
        this.mwTangibleAssetsDao = mwTangibleAssetsDao;
        this.mwPerfromService = mwPerfromService;
        this.mwCommonService = mwCommonService;
        this.b = b;
        this.configType = configType;
    }

    public PerformResultEntity call() {
        try {
            MwTangibleassetsTable param = this.param;
            //1 根据资产查询出 下载配置的cmd命令
            String assetsId = param.getId();
            //ServerEntity engine = mwTangibleAssetsDao.getServerIp(assetsId);
            //String url = engine.getServerIp()+":"+engine.getPort()+"/download";
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
            //查看下载什么类型的配置
            String cmd2 = xmlMaps.get("DownloadConfig");
            String configTypeCmd = xmlMaps.get(configType);
            String cmd = Parser.parse0(cmd2,configTypeCmd);

            String[] moreCmds;
            String enterEnable;

            //判断是否含有restart字段
            String reset = xmlMaps.get("RESET");
            //rest命令是否在第一行执行（1：是，0/null：否）
            String resetFirst = xmlMaps.get("resetFirst");

            String enableCommand = xmlMaps.get("EnableCommand");

            if (StringUtils.isNotEmpty(enableCommand)) {
                enterEnable = enableCommand;
            } else {
                enterEnable = "super" ;
            }


            if(reset!=null && !reset.equals("")){
                String []resets =reset.split("#");
                moreCmds = new String[resets.length+1];
                if ("1".equals(resetFirst)) {
                    for (int i = 0; i < resets.length; i++) {
                        moreCmds[i] = resets[i];
                    }
                    moreCmds[resets.length] = cmd;
                } else {
                    moreCmds[0] = cmd;
                    for (int i = 0; i < resets.length; i++) {
                        moreCmds[i+1] = resets[i];
                    }
                }
            }else {
                moreCmds = new String[1];
                moreCmds[0] =cmd;
            }

            //2 查询出 账号信息
            MwAccountMapper accountMapper = mwTangibleAssetsDao.selectAccountMapper(assetsId);
            QueryAccountManageParam account = mwTangibleAssetsDao.selectOneAccount(accountMapper.getAccountId());
            if(account == null) {
                PerformResultEntity res = PerformResultEntity.builder().ip(param.getInBandIp())
                        .hostName(param.getHostName()).isSuccess(false).results("该设备关联信息出错,无法执行").build();
                return res;
            }


            if (account.getEnable()) {
                String[] tmpArray = moreCmds;
                moreCmds = new String[tmpArray.length + 2];
                moreCmds[0] = enterEnable;
                moreCmds[1] = EncryptUtil.decrypt(account.getEnablePassword());
                for (int i = 0; i < tmpArray.length; i++) {
                    moreCmds[2 + i] = tmpArray[i];
                }
            }
            //3 使用相关协议下载配置
            String protocol = account.getProtocol();
            if("TELNET".equals(protocol)){
                Boolean flag = true;
                String result = mwPerfromService.telent2(param.getInBandIp(),Integer.parseInt(account.getPort()),
                        account.getUsername(),EncryptUtil.decrypt(account.getPassword()),moreCmds);
                if(result.equals("login error") ||result.equals("Connection timed out") ){
                    flag = false;
                }

                //TODO发送请求获取配置，不要本地直接获取
//                HttpEntity entity = new HttpEntity(param.getInBandIp(),Integer.parseInt(account.getPort()),
//                        account.getUsername(),account.getPassword(),xmlMaps.get("DownloadConfig"));
//                String result2 = HttpUtil.post(url, JSONObject.toJSONString(entity));

                //////System.out.println(result);
                //4 保存配置相关信息(配置信息内容太多，单独保存一个txt文件)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String name = param.getHostName()+"_"+configType+"_"+sdf.format(new Date())+"_"+((int)(Math.random()*(9000)+1000));

                if (param.getInBandIp()!=null||param.getInBandIp()!=""){
                    name = param.getHostName()+"_"+configType+"_"+param.getInBandIp()+"_"+sdf.format(new Date())+"_"+((int)(Math.random()*(9000)+1000));
                }

                MwNcmPath mwNcmPath = mwTangibleAssetsDao.getPath();
                String path = mwNcmPath.getDownloadPath();
                String s = savDownloadConfig(name,path,result);

                //System.err.println(s);

                MwNcmDownloadConfig data = new MwNcmDownloadConfig();
                data.setAssetsId(param.getAssetsId());
                data.setConfigType(configType);
                data.setName(name+".mwcfg");
                data.setPath(path);
                data.setCreateDate(new Date());
                if(b == true){
                    data.setCreator(iLoginCacheInfo.getLoginName());
                }
                mwTangibleAssetsDao.saveDownloadConfig(data);
                String pa = path+"/"+name + ".mwcfg";
                PerformResultEntity res = PerformResultEntity.builder().ip(param.getInBandIp())
                        .hostName(param.getHostName()).isSuccess(flag).path(pa).results(result).build();
                return res;
            }
            else if("SSH".equals(protocol)){
                Boolean flag = true;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String result ="";
                String name = param.getHostName()+"_"+configType+"_"+sdf.format(new Date())+"_"+((int)(Math.random()*(9000)+1000));
                if (param.getInBandIp()!=null||param.getInBandIp()!=""){
                    name = param.getHostName()+"_"+configType+"_"+param.getInBandIp()+"_"+sdf.format(new Date())+"_"+((int)(Math.random()*(9000)+1000));
                }
                if (configType.equals("TFTP")){
                    String billName = name;
                    name = param.getHostName()+"("+param.getInBandIp()+")"+"_"+"saved"+"_"+sdf.format(new Date())+"_"+((int)(Math.random()*(9000)+1000));
                    List<String> orgList = mwCommonService.getOrgNameByTypeId(param.getAssetsId(),DataType.ASSETS.getName());
                    String org = "";
                    if (orgList.size()>0){
                        org = orgList.get(0);
                    }
                    org = StringUtils.converterToSpell(org);
                    name = org+"/"+param.getAssetsName()+"/"+name;
                    String [] bill =new String[1];;
                    String[] s = account.getIpDown().split(",");
                    if (s.length>2){
                        bill[0] = "tftp "+ s[0]+" "+name+".cfg "+s[2];
                    }else if (s.length>1){
                        bill[0] = "tftp "+ s[0]+" "+name+".cfg ";
                    }else {
                        bill[0] = "tftp"+name+".cfg ";
                    }

                    result =mwPerfromService.sshDownload(param.getInBandIp(),Integer.parseInt(account.getPort()),
                            account.getUsername(),EncryptUtil.decrypt(account.getPassword()),bill,accountMapper.getDelay());
                    name=billName;
                }else {
                    result =mwPerfromService.sshDownload(param.getInBandIp(),Integer.parseInt(account.getPort()),
                            account.getUsername(),EncryptUtil.decrypt(account.getPassword()),moreCmds,accountMapper.getDelay());
                }

                if(result.equals("ssh cmds is null") || result.equals("ssh login error")){
                    flag = false;
                }
                //System.err.println(result);
                //4 保存配置相关信息(配置信息内容太多，单独保存一个txt文件)
                MwNcmPath mwNcmPath = mwTangibleAssetsDao.getPath();
                String path = mwNcmPath.getDownloadPath();
                String s = savDownloadConfig(name,path,result);
                //System.err.println(s);

                MwNcmDownloadConfig data = new MwNcmDownloadConfig();
                data.setAssetsId(param.getAssetsId());
                data.setConfigType(configType);
                data.setName(name+".mwcfg");
                data.setPath(path);
                data.setCreateDate(new Date());
                if(b == true){
                    data.setCreator(iLoginCacheInfo.getLoginName());
                }
                mwTangibleAssetsDao.saveDownloadConfig(data);
                String pa = path+"/"+name + ".mwcfg";
                PerformResultEntity res = PerformResultEntity.builder().ip(param.getInBandIp())
                        .hostName(param.getHostName()).isSuccess(flag).path(pa).results(result).build();
                return res;
            }
        } catch (Exception e) {
            log.error("执行文本失败",e);
            PerformResultEntity res = PerformResultEntity.builder().ip(param.getInBandIp())
                    .hostName(param.getHostName()).isSuccess(false).results(e.getMessage()).build();
            return res;
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
            fop.write(context);
            fop.flush();
            fop.close();

        } catch (FileNotFoundException e) {
            log.error("执行文本失败",e);
        }catch (IOException e) {
            log.error("执行文本失败",e);
        }finally {
            if(fop != null){
                try {
                    fop.close();
                } catch (IOException e) {
                    log.error("执行文本失败",e);
                }
            }
        }
        return "保存成功";
    }


//    public  String getIsoToUtf_8(String str) {
//        if (StringUtils.isBlank(str)) {
//            return "";
//        }
//        String newStr = "";
//        try {
//            newStr = new String(str.getBytes("ISO8859-1"), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//
//        }
//        return newStr;
//    }

}
