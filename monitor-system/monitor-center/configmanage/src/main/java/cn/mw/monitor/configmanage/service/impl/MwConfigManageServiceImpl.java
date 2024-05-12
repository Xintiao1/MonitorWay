package cn.mw.monitor.configmanage.service.impl;

import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.configmanage.common.ConfigType;
import cn.mw.monitor.configmanage.common.DetectMatchLevel;
import cn.mw.monitor.configmanage.common.DetectType;
import cn.mw.monitor.configmanage.dao.MwConfigManageTableDao;
import cn.mw.monitor.configmanage.entity.*;
import cn.mw.monitor.configmanage.service.EncryptUtil;
import cn.mw.monitor.configmanage.service.MwConfigManageService;
import cn.mw.monitor.configmanage.service.MwPerfromService;
import cn.mw.monitor.configmanage.thread.DetectMatchThread;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.configmanage.ConfigManageCommonService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.templatemanage.entity.QueryTemplateManageParam;
import cn.mw.monitor.timetask.entity.MwTimeTaskDownloadHis;
import cn.mw.monitor.timetask.entity.MwTimeTaskTable;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.ExcelUtils;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang.StringUtils;
import org.dom4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
public class MwConfigManageServiceImpl implements MwConfigManageService, ConfigManageCommonService {

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Resource
    private MwConfigManageTableDao mwConfigManageTableDao;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MwPerfromService mwPerfromService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    private MWUserService mwUserService;

    @Autowired
    private MWCommonService commonService;

    @Value("${screen.image.file.uploadFolder}")
    private String uploadFolder;

    private ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * redis前缀
     */
    private static String DETECT_REDIS_PREFIX = "assets-detect-";
    private static String DETECT_TYPE_REDIS_PREFIX = "assets-detect-type-";
    private static  String CONFIG_TFTP =  "TFTP";

    @Override
    public Reply updateOrAddPath(MwNcmPath param) {
        if (param != null && param.getId() != null) {
           String res = isAb(param);
           if(res==null){
               mwConfigManageTableDao.updatePath(param);
           }else {
               return Reply.fail(res);
           }
        } else {
            String res = isAb(param);
            if(res==null){
                mwConfigManageTableDao.addPath(param);
            }else {
                return Reply.fail(res);
            }
        }
        return Reply.ok(param);
    }

    private String isAb(MwNcmPath param) {
        String path1 = param.getDownloadPath();
        File f1 = new File(path1);
        if(!f1.isAbsolute()){
            return "下载配置文件路径需要绝对路径";
        }
        File f2 = new File(param.getPerfromPath());
        if(!f2.isAbsolute()){
            return "执行脚本文件路径需要绝对路径";
        }
        return null;
    }

    @Override
    public Reply getPath() {
        MwNcmPath path = mwConfigManageTableDao.getPath();
        return Reply.ok(path);
    }

    @Override
    public Reply batchPerform(MwDownloadParam mwDownloadParam) {
        String setMessage = "";
        try {
            List<MwTangibleassetsTable> param = mwDownloadParam.getParam();

            ExecutorService pool = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(param.size());
            List<Future<PerformResultEntity>> results = new ArrayList<>();
            for (MwTangibleassetsTable data : param) {
                Future<PerformResultEntity> res = pool.submit(new MwPerformService(latch, data, iLoginCacheInfo, mwConfigManageTableDao, mwPerfromService, true, mwDownloadParam.getCmds()));
                results.add(res);
            }
            pool.shutdown();
            latch.await();

            //判断返回结果
            int cg = 0;
            int sb = 0;
            StringBuffer resMessage = new StringBuffer();
            for (Future<PerformResultEntity> f : results) {
                try {
                    PerformResultEntity result = f.get(10, TimeUnit.SECONDS);
                    if (result.getIsSuccess()) {
                        cg++;
                    } else {
                        sb++;
                        resMessage.append(result.getIp()).append(result.getResults());
                    }
                } catch (Exception e) {
                    f.cancel(true);
                }
            }
            setMessage = "成功:" + cg + "个,失败:" + sb + "个";
            if (sb >= 1) {
                return Reply.fail(setMessage + "\r\n" + resMessage);
            } else {
                return Reply.ok(setMessage);
            }
        } catch (Exception e) {
            log.error("执行失败",e);
        }
        return Reply.fail(setMessage);
    }

    @Override
    public Reply configPerform(QueryTangAssetsParam param) {
        String assetsId = param.getAssetsId();
        //1 获得要执行的命令；
        String cmds = param.getCmds();
        String[] cmd = cmds.split("\n");

        //2 查询出 账号信息
        MwAccountMapper accountMapper = mwConfigManageTableDao.selectAccountMapper(assetsId);
        QueryAccountManageParam account = mwConfigManageTableDao.selectOneAccount(accountMapper.getAccountId());

        //3 使用相关协议执行命令
        String protocol = account.getProtocol();
        if ("telnet".equals(protocol)) {
            String result = mwPerfromService.telent2(param.getInBandIp(), Integer.parseInt(account.getPort()),
                    account.getUsername(), account.getPassword(), cmd);
            //System.err.println(result);
            //4 保存配置相关信息(配置信息内容太多，单独保存一个txt文件)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String name = param.getHostName() + "_" + sdf.format(new Date()) + "_" + ((int) (Math.random() * (9000) + 1000));

            MwNcmPath mwNcmPath = mwConfigManageTableDao.getPath();
            String path = mwNcmPath.getPerfromPath();
            String s = savDownloadConfig(name, path, result);
            //System.err.println(s);

            MwNcmDownloadConfig data = new MwNcmDownloadConfig();
            data.setAssetsId(param.getAssetsId());
            data.setConfigType(cmds);
            data.setName(name + ".mwcfg");
            data.setPath(path);
            data.setCreateDate(new Date());
            mwConfigManageTableDao.saveConfigPerfrom(data);
        } else if ("SSH".equals(protocol)) {
            //TODO
        }
        return Reply.ok("执行成功");
    }

    @Override
    public Reply configCompare(List<MwNcmDownloadConfig> param) {
        if (param != null && param.size() == 2) {
            for (MwNcmDownloadConfig data : param) {
                String s = showTxt(data.getPath() + "/" + data.getName());
                data.setContext(s);
            }
        }else {
            return Reply.fail("配置对比需要两条数据对比！");
        }
        return Reply.ok(param);
    }

    @Override
    public Reply deleteConfigs(List<MwNcmDownloadConfig> param) {
        mwConfigManageTableDao.deleteDownloads(param);
        for (MwNcmDownloadConfig data : param) {
            String fileName = data.getPath() + "/" + data.getName();
            File file = new File(fileName);
            file.delete();
        }
        return Reply.ok("删除成功！");
    }

    @Override
    public void getDownload(MwNcmDownloadConfig param, HttpServletResponse response) {
        String pathname = param.getPath() + "/" + param.getName();
        String fileName = param.getName();
        String  str = "";
        String newFileName = "";

        if (param.getConfigType().equals("TFTP")){
            String path = uploadFolder+File.separator+"tftp";
            newFileName = fileName.replaceAll(".mwcfg", ".cfg");
            str = showUnPassword(path+ Matcher.quoteReplacement(File.separator)+ param.getName().replaceAll(".mwcfg", ".cfg"));
        }else {
            newFileName = fileName.replaceAll(".mwcfg", ".config");
            str = showTxt(pathname);
        }

        OutputStream os = null;

        response.setContentType("application/force-download");
        response.setHeader("Content-Disposition", "attachment;fileName=" + newFileName);

        try {
            os = response.getOutputStream();
            os.write(str.getBytes("UTF-8"));
            os.close();
            //System.err.println("下载成功");
        } catch (Exception e) {
            log.error("下载失败",e);
        }

    }

    @Override
    public Reply selectDownload(MwNcmDownloadConfig param) {
        String s = showTxt(param.getPath() + "/" + param.getName());
        param.setContext(s);
        //System.err.println(s);
        return Reply.ok(param);

    }

    public String showTxt(String filename) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        String str = "";
        try {
            br = new BufferedReader(new FileReader(filename));
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(System.lineSeparator() + s);
            }

            String result = sb.toString();
            str = EncryptUtil.decrypt(result);
        } catch (Exception e) {
            log.error("下载文本失败",e);
            return e.getMessage();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error("下载文本失败",e);
            }
        }

        return str;
    }

    @Override
    public Reply selectDownloads(MwNcmDownloadConfig param) {
        //先清空多余数据
        /*clearConfigSetting(param.getAssetsId());*/
        PageHelper.startPage(param.getPageNumber(), param.getPageSize());
        Map priCriteria = null;
        try {
            priCriteria = PropertyUtils.describe(param);
        } catch (Exception e) {
            log.error("查看配置失败",e);
        }

        List<MwNcmDownloadConfig> data = mwConfigManageTableDao.selectDownloads(priCriteria);

        PageInfo pageInfo = new PageInfo<>(data);
        pageInfo.setList(data);
        return Reply.ok(pageInfo);
    }

    public void mainThreadOtherWord() {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            log.error("睡眠失败",e);
        }
    }

    //定时管理中 执行脚本执行的方法
    @Override
    public List<Future<PerformResultEntity>> runScript(MwTimeTaskTable p) {
        Date timeStart = new Date();
        //查询出所属配置
        List<MwTangibleassetsTable> param = mwConfigManageTableDao.selectTimeConfig(p.getTiming());
        String cmds = p.getCmds();
        List<Future<PerformResultEntity>> results = new ArrayList<>();
        try {
            ExecutorService pool = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(param.size());
            for (MwTangibleassetsTable data : param) {
                Future<PerformResultEntity> res = pool.submit(new MwPerformService(latch, data, iLoginCacheInfo, mwConfigManageTableDao, mwPerfromService, false, cmds));
                results.add(res);
            }
            mainThreadOtherWord();
            pool.shutdown();
            latch.await();

            //生成汇总html文件
            String[] resHtml = createHtml(results, timeStart);
            int cg = 0;
            int sb = 0;
            for (int i = 0; i < results.size(); i++) {
                Boolean res = results.get(i).get().getIsSuccess();
                if (res) {
                    cg++;
                } else {
                    sb++;
                }
            }
            MwTimeTaskDownloadHis timeHis = new MwTimeTaskDownloadHis();
            timeHis.setDowntime(new Date());
            timeHis.setDownresult("执行脚本成功：" + cg + ",失败：" + sb);
            timeHis.setTimeId(p.getId());
            timeHis.setPath(resHtml[0]);
            timeHis.setName(resHtml[1]);
            mwConfigManageTableDao.saveTimeHis(timeHis);
            //保存记录
        } catch (InterruptedException e) {
            log.error("定时管理中 执行脚本执行的方法失败", e);
        } catch (ExecutionException e) {
            log.error("定时管理中 执行脚本执行的方法失败", e);
        } catch (FileNotFoundException e) {
            log.error("定时管理中 执行脚本执行的方法失败", e);
        }
        return results;
    }

    private String[] createHtml(List<Future<PerformResultEntity>> results, Date start) throws ExecutionException, InterruptedException, FileNotFoundException {
        //晒选成功和失败的信息
        List<PerformResultEntity> cg = new ArrayList<>();
        List<PerformResultEntity> sb = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            PerformResultEntity data = results.get(i).get();
            Boolean res = data.getIsSuccess();
            if (res) {
                cg.add(data);
            } else {
                sb.add(data);
            }
        }

        //用于存储html字符串
        StringBuilder stringHtml = new StringBuilder();

        //处理html文件存储的路径和名称
        MwNcmPath mwNcmPath = mwConfigManageTableDao.getPath();
        String pathPre = mwNcmPath.getPerfromPath();
        Date now = new Date();
        String path = pathPre + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(now) + "巡检汇总.html";

        //先 生成目录
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }

        //html文件流
        PrintStream printStream = new PrintStream(new FileOutputStream(path + "/" + name));

        //输入HTML文件内容
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strStart = sdf.format(start);
        stringHtml.append("<html><head>");
        stringHtml.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        stringHtml.append("<title>巡检脚本</title>");
        stringHtml.append("</head>");
        stringHtml.append("<body>");

        stringHtml.append("<br>___________________________________________________________________________</br>");
        stringHtml.append("<br>" + strStart + "：Started 巡检脚本" + "</br>");
        stringHtml.append("<br>Execute Command Script on Devices</br>");
        stringHtml.append("<br>" + (cg.size() + sb.size()) + " devices selected</br>");
        stringHtml.append("<br></br>");
        stringHtml.append("<br>Devices: " + (cg.size() + sb.size()) + "</br>");
        stringHtml.append("<br>Errors: " + sb.size() + "</br>");
        stringHtml.append("<br>___________________________________________________________________________</br>");
        for (PerformResultEntity entity : sb) {
            stringHtml.append("<br>" + entity.getHostName() + "(" + entity.getIp() + ")" + "</br>");
            stringHtml.append("<br>" + "ERROR:" + entity.getResults() + "</br>");
            stringHtml.append("<br>___________________________________________________________________________</br>");
        }
        for (PerformResultEntity entity : cg) {
            stringHtml.append("<br>" + entity.getHostName() + "(" + entity.getIp() + ")" + "</br>");
            stringHtml.append("<pre>" + entity.getResults() + "</pre>");
            stringHtml.append("<br>___________________________________________________________________________</br>");

        }
        Date endTime = new Date();
        String strEnd = sdf.format(endTime);
        long a = endTime.getTime() - start.getTime();
        stringHtml.append("<br>" + strEnd + "：Completed 巡检脚本" + "</br>");
        stringHtml.append("<br>Execution time : " + formatTime(a) + "</br>");
        stringHtml.append("<br>___________________________________________________________________________</br>");
        stringHtml.append("</body></html>");
        printStream.println(stringHtml.toString());
        printStream.close();

        String[] returnRes = new String[]{path, name};
        return returnRes;
    }

    private String[] createHtml2(List<PerformResultEntity> results, Date start) throws ExecutionException, InterruptedException, FileNotFoundException {
        //晒选成功和失败的信息
        List<PerformResultEntity> cg = new ArrayList<>();
        List<PerformResultEntity> sb = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            PerformResultEntity data = results.get(i);
            Boolean res = data.getIsSuccess();
            if (res) {
                cg.add(data);
            } else {
                sb.add(data);
            }
        }

        //用于存储html字符串
        StringBuilder stringHtml = new StringBuilder();

        //处理html文件存储的路径和名称
        MwNcmPath mwNcmPath = mwConfigManageTableDao.getPath();
        String pathPre = mwNcmPath.getDownloadPath();
        Date now = new Date();
        String path = pathPre + "/" + new SimpleDateFormat("yyyy-MM-dd").format(now);
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(now) + "配置汇总.html";

        //先 生成目录
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }

        //html文件流
        PrintStream printStream = new PrintStream(new FileOutputStream(path + "/" + name));

        //输入HTML文件内容
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strStart = sdf.format(start);
        stringHtml.append("<html><head>");
        stringHtml.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        stringHtml.append("<title>汇总</title>");
        stringHtml.append("</head>");
        stringHtml.append("<body>");

        stringHtml.append("<br>___________________________________________________________________________</br>");
        stringHtml.append("<br>" + strStart + "：Started 配置备份" + "</br>");
        stringHtml.append("<br>Execute Command Script on Devices</br>");
        stringHtml.append("<br>" + (cg.size() + sb.size()) + " devices selected</br>");
        stringHtml.append("<br></br>");
        stringHtml.append("<br>Devices: " + (cg.size() + sb.size()) + "</br>");
        stringHtml.append("<br>Errors: " + sb.size() + "</br>");
        stringHtml.append("<br>___________________________________________________________________________</br>");
        for (PerformResultEntity entity : sb) {
            stringHtml.append("<br>" + entity.getHostName() + "(" + entity.getIp() + ")" + "</br>");
            stringHtml.append("<br>" + "ERROR:" + entity.getResults() + "</br>");
            stringHtml.append("<br>___________________________________________________________________________</br>");
        }
        for (PerformResultEntity entity : cg) {
            stringHtml.append("<br>" + entity.getHostName() + "(" + entity.getIp() + ")" + "</br>");
            stringHtml.append("<pre>" + entity.getResults() + "</pre>");
            stringHtml.append("<br>___________________________________________________________________________</br>");

        }
        Date endTime = new Date();
        String strEnd = sdf.format(endTime);
        long a = endTime.getTime() - start.getTime();
        stringHtml.append("<br>" + strEnd + "：Completed 配置备份" + "</br>");
        stringHtml.append("<br>Execution time : " + formatTime(a) + "</br>");
        stringHtml.append("<br>___________________________________________________________________________</br>");
        stringHtml.append("</body></html>");
        printStream.println(stringHtml.toString());
        printStream.close();

        String[] returnRes = new String[]{path, name};
        return returnRes;
    }

    /**
     * @describe 将毫秒数转换成一个合适的输出字符串
     */
    public String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "天");
        }
        if (hour > 0) {
            sb.append(hour + "小时");
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second > 0) {
            sb.append(second + "秒");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + "毫秒");
        }
        return sb.toString();
    }

    //定时管理中 配置备份执行的方法
    //@Async
    public List<PerformResultEntity> runMethod(MwTimeTaskTable p) {
        Date timeStart = new Date();

        //查询出所属配置
        List<MwTangibleassetsTable> param = mwConfigManageTableDao.selectTimeConfig(p.getTiming());
        List<PerformResultEntity> results = new ArrayList<>();
        try {
            ExecutorService pool = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(param.size());
            for (MwTangibleassetsTable data : param) {
                MwDownloadService mwDownloadService = new MwDownloadService( data, iLoginCacheInfo, mwConfigManageTableDao, mwPerfromService, false, p.getConfigType(),commonService);
                PerformResultEntity res = mwDownloadService.call();
                results.add(res);
            }
//            mainThreadOtherWord();
//            pool.shutdown();
//            latch.await();

            //生成汇总html文件
            String[] resHtml = createHtml2(results, timeStart);

            int cg = 0;
            int sb = 0;
            for (int i = 0; i < results.size(); i++) {
                Boolean res = results.get(i).getIsSuccess();
                if (res) {
                    cg++;
                } else {
                    sb++;
                }
            }
            MwTimeTaskDownloadHis timeHis = new MwTimeTaskDownloadHis();
            timeHis.setDowntime(new Date());
            timeHis.setDownresult("配置备份成功：" + cg + ",失败：" + sb);
            timeHis.setTimeId(p.getId());
            timeHis.setPath(resHtml[0]);
            timeHis.setName(resHtml[1]);
            mwConfigManageTableDao.saveTimeHis(timeHis);
            //保存记录
        } catch (Exception e) {
            log.error("定时管理中 配置备份执行的方法失败",e);
        }
        return results;
    }

    @Override
    public Reply batchDownload(MwDownloadParam d) {
        String setMessage = "";
//        try {
            String configType = d.getConfigType();
            List<MwTangibleassetsTable> param = d.getParam();
//            ExecutorService pool = Executors.newFixedThreadPool(5);
//            CountDownLatch latch = new CountDownLatch(param.size());
//            List<Future<PerformResultEntity>> results = new ArrayList<>();
//            for (MwTangibleassetsTable data : param) {
//                Future<PerformResultEntity> res = pool.submit(new MwDownloadService(latch, data, iLoginCacheInfo, mwTangibleAssetsDao, mwPerfromService, true, configType));
//                results.add(res);
//            }
////            mainThreadOtherWord();
//            pool.shutdown();
//            latch.await();
//
//            //判断返回结果
//            int cg = 0;
//            int sb = 0;
//            StringBuffer resMessage = new StringBuffer();
//            for (Future<PerformResultEntity> f : results) {
//                try {
//                    PerformResultEntity result = f.get(10, TimeUnit.SECONDS);
//                    if (result.getIsSuccess()) {
//                        cg++;
//                    } else {
//                        sb++;
//                        resMessage.append(result.getIp()).append(result.getResults());
//                    }
//                } catch (Exception e) {
//                    f.cancel(true);
//                }
//            }
//            setMessage = "成功:" + cg + "个,失败:" + sb + "个";
//            if (sb >= 1) {
//                return Reply.fail(setMessage + "\r\n" + resMessage);
//            } else {
//                return Reply.ok(setMessage);
//            }

            List<PerformResultEntity> results = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(param.size());
            for (MwTangibleassetsTable data : param) {
                MwDownloadService mwDownloadService = new MwDownloadService( data, iLoginCacheInfo, mwConfigManageTableDao, mwPerfromService, true, configType,commonService);
                PerformResultEntity res = mwDownloadService.call();
                results.add(res);
            }

            //判断返回结果
            int cg = 0;
            int sb = 0;
            StringBuffer resMessage = new StringBuffer();
            for (PerformResultEntity f : results) {

                    PerformResultEntity result = f;
                    if (result.getIsSuccess()) {
                        cg++;
                    } else {
                        sb++;
                        resMessage.append(result.getIp()).append(result.getResults());
                    }

            }
            setMessage = "成功:" + cg + "个,失败:" + sb + "个";
            if (sb >= 1) {
                return Reply.fail(setMessage + "\r\n" + resMessage);
            } else {
                return Reply.ok(setMessage);
            }

//        } catch (InterruptedException e) {
//
//        }
//        return Reply.fail(setMessage);
    }

    @Override
    public Reply download(QueryTangAssetsParam param) {
        try {
            //1 根据资产查询出 下载配置的cmd命令
            String assetsId = param.getAssetsId();
            MwTemplateMapper templateMapper = mwConfigManageTableDao.selectTemplateMapper(assetsId);
            QueryTemplateManageParam template = mwConfigManageTableDao.selectOneTemplate(templateMapper.getTemplateId());
            Map<String, String> xmlMaps = new HashMap<>();
            String xml = template.getXml();
            Document doc = DocumentHelper.parseText(xml);
            XPath xPath = DocumentHelper.createXPath("//Command");
            List nodes = xPath.selectNodes(doc);
            for (Object obj : nodes) {
                Element n = (Element) obj;
                String key = n.attribute("Name").getText();
                String value = n.attribute("Value").getText();
                xmlMaps.put(key, value);
            }
            //System.err.println(xmlMaps);

            //2 查询出 账号信息
            MwAccountMapper accountMapper = mwConfigManageTableDao.selectAccountMapper(assetsId);
            QueryAccountManageParam account = mwConfigManageTableDao.selectOneAccount(accountMapper.getAccountId());

            //3 使用相关协议下载配置
            String protocol = account.getProtocol();
            if ("telnet".equals(protocol)) {
                String result = mwPerfromService.telent1(param.getInBandIp(), Integer.parseInt(account.getPort()),
                        account.getUsername(), account.getPassword(), xmlMaps.get("DownloadConfig"));
                //System.err.println(result);
                //4 保存配置相关信息(配置信息内容太多，单独保存一个txt文件)
                String name = UUID.randomUUID().toString().replace("-", "").toUpperCase();

                MwNcmPath mwNcmPath = mwConfigManageTableDao.getPath();
                String path = mwNcmPath.getDownloadPath();
                String s = savDownloadConfig(name, path, result);
                //System.err.println(s);

                MwNcmDownloadConfig data = new MwNcmDownloadConfig();
                data.setAssetsId(param.getAssetsId());
                data.setConfigType(null);
                data.setName(name + ".mwcfg");
                data.setPath(path);
                data.setCreateDate(new Date());
                data.setCreator(iLoginCacheInfo.getLoginName());
                mwConfigManageTableDao.saveDownloadConfig(data);
            }
        } catch (DocumentException e) {
            log.error("下载文本失败",e);
        }
        return null;
    }

    private String savDownloadConfig(String name, String path, String result) {
        FileOutputStream fop = null;
        try {
            //创建目录
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }

            File file = new File(path, name + ".mwcfg");
            fop = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            String str = EncryptUtil.encrypt(result);
            byte[] context = str.getBytes();
            fop.write(context);
            fop.flush();
            fop.close();

        } catch (Exception e) {
            log.error("保存文件失败",e);
        }finally {
            if (fop != null) {
                try {
                    fop.close();
                } catch (IOException e) {
                    log.error("保存文件失败",e);
                }
            }
        }
        return "保存成功";
    }


    @Override
    public Reply editor(MwConfigMapper param) {
        editAssets(param);
        return Reply.ok(param);
    }


    /**
     * 批量更新
     * @param param 配置参数
     * @return
     */
    @Override
    public Reply batchEditor(MwConfigMapper param) {
        if (CollectionUtils.isEmpty(param.getAssetsIds())){
            return  Reply.fail("无法获取资产数据");
        }
        param.getAssetsIds().forEach(assetsId->{
            param.setAssetsId(assetsId);
            param.setId(assetsId);
            param.setTiming(param.getTiming());
            editAssets(param);
        });
        return Reply.ok(param);
    }


    /**
     * 编辑资产关联关系
     * @param param 配置信息
     */
    private void editAssets(MwConfigMapper param) {
        param.getMwAccountMapper().setCreateDate(new Date());
        param.getMwAccountMapper().setCreator(iLoginCacheInfo.getLoginName());
        param.getMwAccountMapper().setModificationDate(new Date());
        param.getMwAccountMapper().setModifier(iLoginCacheInfo.getLoginName());
        param.getMwAccountMapper().setAssetsId(param.getAssetsId());
        //保存账号
        mwConfigManageTableDao.deleteAccountMapper(param.getAssetsId());
        if (param.getDelay()==0){
            param.setDelay(200);
            param.getMwAccountMapper().setDelay(200);
        }
        else {
            param.getMwAccountMapper().setDelay(param.getDelay());
        }
        mwConfigManageTableDao.saveAccountMapper(param.getMwAccountMapper());

        param.getMwTemplateMapper().setAssetsId(param.getAssetsId());
        param.getMwTemplateMapper().setCreateDate(new Date());
        param.getMwTemplateMapper().setCreator(iLoginCacheInfo.getLoginName());
        param.getMwTemplateMapper().setModificationDate(new Date());
        param.getMwTemplateMapper().setModifier(iLoginCacheInfo.getLoginName());
        //保存模板
        mwConfigManageTableDao.deleteTemplateMapper(param.getAssetsId());
        mwConfigManageTableDao.saveTemplateMapper(param.getMwTemplateMapper());

        //编辑定时间隔
        mwConfigManageTableDao.updateTiming(param);
    }



    @Override
    public Reply editorSelect(MwConfigMapper qPram) {
        MwConfigMapper result = mwConfigManageTableDao.editorSelect(qPram.getAssetsId());
        //获取账户信息
        MwAccountMapper account = mwConfigManageTableDao.selectAccount(qPram.getAssetsId());
        MwTemplateMapper template = mwConfigManageTableDao.selectTemplate(qPram.getAssetsId());
        if (account==null){
            result.setDelay(200);
        }else {
            result.setDelay(account.getDelay());
        }
        result.setMwAccountMapper(account);
        result.setMwTemplateMapper(template);
        return Reply.ok(result);
    }

    /**
     * 查询资产信息list
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply selectList(QueryTangAssetsParam qParam) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<String> idList = mwUserService.getAllTypeIdList(userInfo,DataType.ASSETS);
            qParam.setSettingFlag(true);
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            List mwTangAssetses = new ArrayList();
            if (qParam.getIsSelectLabel()) {//高级查询
                QueryLabelParam labelParam = new QueryLabelParam();
                labelParam.setAssetsTypeId(qParam.getAssetsTypeId());
                // 获取本次查询的标签所有的标签值
                List<MwAllLabelDTO> allLabel = mwConfigManageTableDao.selectAllLabel(labelParam);
                qParam.setAllLabelList(allLabel);
                mwTangAssetses = mwConfigManageTableDao.selectLabelList(qParam);
                PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
                pageInfo.setList(mwTangAssetses);
            } else {//普通查询
                Map criteria = PropertyUtils.describe(qParam);
                criteria.put("isSystem",userInfo.isSystemUser());
                criteria.put("listSet",Joiner.on(",").join(idList));
                mwTangAssetses = mwConfigManageTableDao.selectList(criteria);
            }
            PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
            pageInfo.setList(mwTangAssetses);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取配置管理数据失败",e);
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210102, ErrorConstant.TANGASSETS_MSG_210102);
        } finally {
            DataPermUtil.remove();
        }
    }


    @Override
    public Reply selectPerforms(MwNcmDownloadConfig qParam) {

        //先清空多余数据
        /*clearConfigSetting(qParam.getAssetsId());*/

        PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
        Map priCriteria = null;

        try {
            priCriteria = PropertyUtils.describe(qParam);
        } catch (Exception e) {
            log.error("查看执行结果失败",e);
        }

        List<MwNcmDownloadConfig> data = mwConfigManageTableDao.selectPerforms(priCriteria);
        if (qParam.getName()!=null){
           List<MwNcmDownloadConfig> datekill = data.stream().filter(e -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(e.getCreateDate()).contains(qParam.getName()) || e.getName().contains(qParam.getName())||e.getConfigType().contains(qParam.getName())||e.getAssetsId().contains(qParam.getName())).collect(Collectors.toList());
            data.clear();
            data.addAll(datekill);
        }
        PageInfo pageInfo = new PageInfo<>(data);

        pageInfo.setList(data);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply showPerform(MwNcmDownloadConfig qParam) {
        String s = showTxt(qParam.getPath() + "/" + qParam.getName());
        qParam.setContext(s);
        return Reply.ok(qParam);
    }

    @Override
    public Reply deletePerforms(List<MwNcmDownloadConfig> qParam) {
        //mwTangibleAssetsDao.deleteDownloads(qParam);
        mwConfigManageTableDao.deletePerforms(qParam);
        for (MwNcmDownloadConfig data : qParam) {
            String fileName = data.getPath() + "/" + data.getName();
            File file = new File(fileName);
            file.delete();
        }
        return Reply.ok("删除成功！");
    }

    @Override
    public void getPerform(MwNcmDownloadConfig qParam, HttpServletResponse response) {
        String pathname = qParam.getPath() + "/" + qParam.getName();
        String fileName = qParam.getName();
        String newFileName = fileName.replaceAll(".mwcfg", ".config");
        String str = showTxt(pathname);
        OutputStream os = null;

        response.setContentType("application/force-download");
        response.setHeader("Content-Disposition", "attachment;fileName=" + newFileName);

        try {
            os = response.getOutputStream();
            os.write(str.getBytes("UTF-8"));
        } catch (Exception e) {
            log.error("下载执行结果失败",e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error("下载执行结果失败",e);
            }
        }
    }

    @Override
    public Reply delete(List<MwTangibleassetsDTO> qParam) {
        if(qParam!=null && qParam.size()>0){
            for (MwTangibleassetsDTO param : qParam) {
                List<MwNcmDownloadConfig> list1 = mwConfigManageTableDao.selectDownloads2(param);
                if(list1!=null && list1.size()>0){
                    deleteConfigs(list1);
                }

                List<MwNcmDownloadConfig> list2 = mwConfigManageTableDao.selectDownloads2(param);
                if(list2!=null && list2.size()>0){
                    deleteConfigs(list2);
                }

                mwConfigManageTableDao.updateConfig(param.getId());
            }
        }
        return Reply.ok();
    }

    @Override
    public Reply getTreeGroup(String treeName) {
        List<MwConfigManageTreeGroup> list = mwConfigManageTableDao.getTreeGroup(treeName);
        List<MwConfigManageTreeGroup> listGroup = groupTree(list,0);

        return Reply.ok(listGroup);
    }

    @Override
    public Reply addTreeGroup(MwConfigManageTreeGroup mwConfigManageTreeGroup) {
        mwConfigManageTableDao.addTreeGroup(mwConfigManageTreeGroup);
        return Reply.ok("新增成功");
    }

    @Override
    public Reply updateTreeGroup(MwConfigManageTreeGroup mwConfigManageTreeGroup) {
        if (mwConfigManageTreeGroup.getName()==null||mwConfigManageTreeGroup.getName().equals("")){
            return Reply.fail("分组名称不能为空");
        }
        mwConfigManageTableDao.updateTreeGroup(mwConfigManageTreeGroup);
        return Reply.ok("修改成功");
    }

    @Override
    public Reply deleteTreeGroup(MwConfigManageTreeGroup mwConfigManageTreeGroup) {
        if (mwConfigManageTreeGroup.getId()==null){
            return Reply.fail("分组id不能为空");
        }
        mwConfigManageTableDao.deleteTreeGroup(mwConfigManageTreeGroup.getId());
        return Reply.ok("删除成功");
    }


    /**
     * 获取规则管理数据列表
     *
     * @param qParam 规则管理数据
     * @return
     */
    @Override
    public Reply getRuleList(MwConfigManageRuleManage qParam) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            //获取当前用户可以看到的所有规则ID
            List<String> idList = mwUserService.getAllTypeIdList(userInfo, qParam.getBaseDataType());
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            Map pubCriteria = PropertyUtils.describe(qParam);
            pubCriteria.put("isSystem", userInfo.isSystemUser());
            pubCriteria.put("listSet", Joiner.on(",").join(idList));
            List<MwConfigManageRuleManage> list = mwConfigManageTableDao.getRuleList(pubCriteria);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取规则列表数据失败", e);
            return Reply.fail("获取规则列表数据失败");
        }
    }

    /**
     * 获取规则详情
     *
     * @param ruleManage 规则管理数据
     * @return 规则详情
     */
    @Override
    public Reply getRuleDetail(MwConfigManageRuleManage ruleManage) {
        //获取规则数据
        RuleManage rule = mwConfigManageTableDao.getRuleManageById(ruleManage.getId());
        MwConfigManageRuleManage resultRule = CopyUtils.copy(MwConfigManageRuleManage.class,rule);
        //获取高级配置数据
        List<MwRuleSelectParam> ruleSelectList = mwConfigManageTableDao.selectMwAlertRuleSelect(DetectType.RULE.getDetectName() + "-" +ruleManage.getId());
        List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
        if(ruleSelectList != null && ruleSelectList.size() > 0){
            for (MwRuleSelectParam s : ruleSelectList){
                if(s.getKey().equals("root")){
                    ruleSelectParams.add(s);
                }
            }
            for(MwRuleSelectParam s : ruleSelectParams){
                s.setConstituentElements(getChild(s.getKey(),ruleSelectList));
            }
            resultRule.setSeniorMatchRuleList(ruleSelectParams);
        }
        //获取数据权限
        cn.mw.monitor.bean.DataPermission permission = commonService.getDataPermission(ruleManage);
        if (permission != null) {
            resultRule.setPrincipal(permission.getUserIds());
            resultRule.setOrgIds(permission.getOrgNodes());
            resultRule.setGroupIds(permission.getGroupIds());
        }
        return Reply.ok(resultRule);
    }

    /**
     * 增加检测报告信息
     *
     * @param detectReport 检测报告
     * @return
     */
    @Override
    @Transactional
    public Reply addDetectReport(DetectReportDTO detectReport) {
        if (StringUtils.isEmpty(detectReport.getReportName())) {
            return Reply.fail("请填写报告名称");
        }
        if (detectReport.getReportTreeGroup() == null || detectReport.getReportTreeGroup() == 0) {
            return Reply.fail("请填写报告所在文件夹");
        }
        if (CollectionUtils.isEmpty(detectReport.getPolicyList())){
            return Reply.fail("请选择策略");
        }
        //增加报告信息
        DetectReport report = CopyUtils.copy(DetectReport.class, detectReport);
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        report.setCreator(userInfo.getLoginName());
        report.setUpdater(userInfo.getLoginName());
        mwConfigManageTableDao.addDetectReport(report);
        detectReport.setId(report.getId());
        //增加数据权限
        commonService.addMapperAndPerm(detectReport);
        //增加检测报告对应关系
        List<Integer> list = new ArrayList<>();
        for (String id : detectReport.getPolicyList()) {
            list.add(Integer.parseInt(id));
        }
        if (CollectionUtils.isNotEmpty(list)){
            mwConfigManageTableDao.addDetectRelation(report.getId(), list, DetectType.REPORT.getDetectName());
            //执行扫描
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    execAssetsDetect(report.getId());
                }
            });
            thread.start();
        }
        return Reply.ok("增加报告成功");
    }

    /**
     * 更新检测报告数据
     *
     * @param detectReport 检测报告
     * @return
     */
    @Override
    @Transactional
    public Reply updateDetectReport(DetectReportDTO detectReport) {
        if (StringUtils.isEmpty(detectReport.getReportName())) {
            return Reply.fail("请填写报告名称");
        }
        if (detectReport.getReportTreeGroup() == null || detectReport.getReportTreeGroup() == 0) {
            return Reply.fail("请填写报告所在文件夹");
        }
        if (CollectionUtils.isEmpty(detectReport.getPolicyList())){
            return Reply.fail("请选择策略");
        }
        //更新报告
        DetectReport report = CopyUtils.copy(DetectReport.class, detectReport);
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        report.setUpdater(userInfo.getLoginName());
        //获取报告数据
        DetectReport oldReport = mwConfigManageTableDao.getDetectReportById(detectReport.getId());
        int unHandleNumber = mwConfigManageTableDao.countUnHandle(oldReport.getReportUUID());
        //如果已经在执行中，则不更新
        if (!oldReport.getReportState() && unHandleNumber != 0) {
            return Reply.ok("报告执行中，请执行结束后重试");
        }
        mwConfigManageTableDao.updateDetectReport(report);
        //更新关联关系（先删后增）
        mwConfigManageTableDao.deleteDetectRelation(detectReport.getId(), DetectType.REPORT.getDetectName());
        //增加检测报告对应关系
        List<Integer> list = new ArrayList<>();
        for (String id : detectReport.getPolicyList()) {
            list.add(Integer.parseInt(id));
        }
        if (CollectionUtils.isNotEmpty(list)) {
            mwConfigManageTableDao.addDetectRelation(detectReport.getId(), list, DetectType.REPORT.getDetectName());
        }
        //更新数据权限
        commonService.updateMapperAndPerm(detectReport);
        return Reply.ok("更新成功！");
    }

    /**
     * 更新检测报告数据
     *
     * @param detectReport 检测报告
     * @return
     */
    @Override
    public Reply updateDetectReportState(DetectReportDTO detectReport) {
        //更新报告
        DetectReport report = CopyUtils.copy(DetectReport.class, detectReport);
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        report.setUpdater(userInfo.getLoginName());
        //获取报告数据
        DetectReport oldReport = mwConfigManageTableDao.getDetectReportById(detectReport.getId());
        //如果已经在执行中，则不执行
        if (!oldReport.getReportState()) {
            return Reply.ok("执行成功！");
        }
        report.setReportState(false);
        mwConfigManageTableDao.updateDetectReportState(report);
        //执行扫描
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                execAssetsDetect(report.getId());
            }
        });
        thread.start();
        return Reply.ok("更新报告成功");
    }

    /**
     * 删除检测报告
     *
     * @param detectReport 检测报告
     * @return
     */
    @Override
    @Transactional
    public Reply deleteDetectReport(DetectReportDTO detectReport) {
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        //批量删除报告
        mwConfigManageTableDao.batchDeleteDetectReport(userInfo.getLoginName(),detectReport.getIds());
        //批量删除关联关系
        mwConfigManageTableDao.batchDeleteDetectRelation(detectReport.getIds(), DetectType.REPORT.getDetectName());
        //删除数据权限
        List<String> deleteList = new ArrayList<>();
        for (Integer id : detectReport.getIds()) {
            deleteList.add(id + "");
        }
        detectReport.setDeleteIdList(deleteList);
        commonService.deleteMapperAndPerm(detectReport);
        return Reply.ok("删除报告成功");
    }

    /**
     * 获取报告列表
     *
     * @param detectReport 报告参数
     * @return
     */
    @Override
    public Reply getReportList(DetectReportDTO detectReport) {
        //根据文件夹ID获取
        try {
            //获取当前用户可以看到的所有规则ID
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<String> idList = mwUserService.getAllTypeIdList(userInfo, detectReport.getBaseDataType());
            detectReport.setSystemUser(userInfo.isSystemUser());
            detectReport.setFindInSet(Joiner.on(",").join(idList));
            PageHelper.startPage(detectReport.getPageNumber(), detectReport.getPageSize());
            List<DetectReport> reportList = mwConfigManageTableDao.listDetectReport(detectReport);
            for (DetectReport report : reportList){
                if (report.getReportState()){
                    continue;
                }
                //判断当前报告是否已经执行完成，如果执行完成则改变状态
                int unHandleNumber = mwConfigManageTableDao.countUnHandle(report.getReportUUID());
                if (unHandleNumber == 0){
                    //更新状态
                    report.setReportState(true);
                    mwConfigManageTableDao.updateDetectReportState(report);
                }
            }
            List<DetectReportDTO> list = CopyUtils.copyList(DetectReportDTO.class, reportList);
            PageInfo pageInfo = new PageInfo<>(reportList);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取报告失败", e);
            return Reply.fail("获取报告失败");
        }
    }

    /**
     * 获取报告详情
     *
     * @param detectReport
     * @return
     */
    @Override
    public Reply getReportDetail(DetectReportDTO detectReport) {
        //根据文件夹ID获取
        try {
            DetectReport report = mwConfigManageTableDao.getDetectReportById(detectReport.getId());
            DetectReportDTO detectReportDTO = CopyUtils.copy(DetectReportDTO.class, report);
            //获取报告对应的策略
            List<Integer> relationList = mwConfigManageTableDao.listDetectRelation(detectReport.getId(), DetectType.REPORT.getDetectName());
            List<String> policyList = new ArrayList<>();
            for (int policyId : relationList) {
                policyList.add(DetectType.POLICY.getDetectName() + "-" + policyId);
            }
            detectReportDTO.setPolicyList(policyList);
            //获取数据权限
            cn.mw.monitor.bean.DataPermission permission = commonService.getDataPermission(detectReport);
            if (permission != null) {
                detectReportDTO.setPrincipal(permission.getUserIds());
                detectReportDTO.setOrgIds(permission.getOrgNodes());
                detectReportDTO.setGroupIds(permission.getGroupIds());
            }
            return Reply.ok(detectReportDTO);
        } catch (Exception e) {
            log.error("获取报告失败", e);
            return Reply.fail("获取报告失败");
        }
    }

    /**
     * 获取报告详情
     *
     * @param detectReport 检测报告
     * @return 报告详情
     */
    @Override
    public Reply getReportDetectDetail(DetectReportDTO detectReport) {
        //返回结果数据
        Map<String, Object> resultMap = new HashMap<>();
        //资产列表
        List<Map<String, Object>> assetsList = new ArrayList<>();
        //栏目列表
        List<Map> typeList = new ArrayList<>();
        //返回数据信息
        PageInfo pageInfo = new PageInfo();
        //获取当前报告是否存在UUID
        DetectReport report = mwConfigManageTableDao.getDetectReportById(detectReport.getId());
        if (report == null || StringUtils.isEmpty(report.getReportUUID())) {
            return Reply.ok(detectReturnFail(report));
        }
        //redis存放的KEY
        String redisKey = DETECT_REDIS_PREFIX + report.getReportUUID();
        String typeRedisKey = DETECT_TYPE_REDIS_PREFIX + report.getReportUUID();
        //获取当前报告是否存在未完成的数据
        int allSize = mwConfigManageTableDao.countReportExec(report.getReportUUID());
        int finishedSize = mwConfigManageTableDao.countFinishedReportExec(report.getReportUUID());
        if (allSize != finishedSize) {
            return Reply.fail("报告未完成，当前检测进度为" + finishedSize + "/" + allSize);
        }
        //若存在缓存，则直接从缓存取
        if (redisTemplate.hasKey(redisKey)) {
            assetsList = (List<Map<String, Object>>) redisTemplate.opsForValue().get(redisKey);
            typeList = (List<Map>) redisTemplate.opsForValue().get(typeRedisKey);
        } else {
            //获取执行记录列表
            List<DetectExecLog> logList = mwConfigManageTableDao.listLogByUUID(report.getReportUUID());
            List<Integer> allRuleIdList = new ArrayList<>();
            getReportTypeList(report, typeList, allRuleIdList);
            Map<String, Integer> assetsIndexMap = new HashMap<>();
            //获取所有的资产数据
            assetsList = mwConfigManageTableDao.listAssetsByReportUUID(report.getReportUUID());
            int index = 0;
            for (Map assets : assetsList) {
                assetsIndexMap.put(assets.get("assetsId").toString(), index);
                index++;
            }
            for (DetectExecLog execLog : logList) {
                if (!assetsIndexMap.containsKey(execLog.getAssetsId())) {
                    continue;
                }
                Map assets = assetsList.get(assetsIndexMap.get(execLog.getAssetsId()));
                assets.put(DetectType.RULE.getDetectName() + "-" + execLog.getRuleId(), execLog.getRuleLevel());
            }
            //校验是否存在未添加的数据
            reviewAssetsData(assetsList, allRuleIdList);
            //将匹配数据存入redis(存储时长为一个小时)
            redisTemplate.opsForValue().set(redisKey, assetsList, 60 * 60 * 1000, TimeUnit.MILLISECONDS);
            redisTemplate.opsForValue().set(typeRedisKey, typeList, 60 * 60 * 1000, TimeUnit.MILLISECONDS);
        }
        //分页
        List newList = new ArrayList();
        if (detectReport.getPageSize() * detectReport.getPageNumber() > assetsList.size()) {
            newList = assetsList.subList((detectReport.getPageNumber() - 1) * detectReport.getPageSize(),
                    assetsList.size());
        } else {
            newList = assetsList.subList((detectReport.getPageNumber() - 1) * detectReport.getPageSize(),
                    detectReport.getPageSize() * detectReport.getPageNumber());
        }
        pageInfo.setList(newList);
        pageInfo.setTotal(assetsList.size());
        int infoCount = mwConfigManageTableDao.countReportDetect(report.getReportUUID(), 0);
        int warnCount = mwConfigManageTableDao.countReportDetect(report.getReportUUID(), 1);
        int errorCount = mwConfigManageTableDao.countReportDetect(report.getReportUUID(), 2);
        int normalCount = mwConfigManageTableDao.countReportDetect(report.getReportUUID(), 3);
        resultMap.put("reportName", report.getReportName());
        resultMap.put("reportDesc", report.getReportDesc());
        resultMap.put("reportCreator", report.getCreator());
        resultMap.put("reportId", report.getId());
        resultMap.put("reportUpdateDate", report.getUpdateDate());
        resultMap.put("infoCount", infoCount);
        resultMap.put("warnCount", warnCount);
        resultMap.put("errorCount", errorCount);
        resultMap.put("normalCount", normalCount);
        resultMap.put("assetsCount", assetsList.size());
        resultMap.put("detectCount", infoCount + warnCount + errorCount + normalCount);
        resultMap.put("typeList", typeList);
        resultMap.put("assetsList", pageInfo);
        return Reply.ok(resultMap);
    }

    /**
     * 扫描失败返回数据
     *
     * @param report 报告数据
     * @return
     */
    private Map<String, Object> detectReturnFail(DetectReport report) {
        Map<String, Object> resultMap = new HashMap<>();
        if (report == null) {
            return resultMap;
        }
        DetectReportDTO detectReport = CopyUtils.copy(DetectReportDTO.class, report);
        //栏目列表
        List<Map> typeList = new ArrayList<>();
        //返回数据信息
        PageInfo pageInfo = new PageInfo();
        getReportTypeList(report, typeList, new ArrayList<>());
        resultMap.put("reportName", report.getReportName());
        resultMap.put("reportDesc", report.getReportDesc());
        resultMap.put("reportCreator", report.getCreator());
        resultMap.put("reportId", report.getId());
        resultMap.put("reportUpdateDate", report.getUpdateDate());
        resultMap.put("typeList", typeList);
        resultMap.put("assetsList", pageInfo);
        return resultMap;
    }

    /**
     * 获取报告栏目列表数据
     *
     * @param detectReport  报告数据
     * @param typeList      栏目列表数据
     * @param allRuleIdList 报告对应的规则ID列表
     */
    private void getReportTypeList(DetectReport detectReport, List<Map> typeList, List<Integer> allRuleIdList) {
        //获取所有的策略ID
        List<Integer> policyIdList = mwConfigManageTableDao.listDetectRelation(detectReport.getId(), DetectType.REPORT.getDetectName());
        if (CollectionUtils.isEmpty(policyIdList)) {
            return;
        }
        //获取所有的策略数据
        PolicyManageDTO policyManageDTO = new PolicyManageDTO();
        policyManageDTO.setIds(policyIdList);
        List<PolicyManage> policyList = mwConfigManageTableDao.listPolicyManage(policyManageDTO);
        for (PolicyManage policyManage : policyList) {
            Map policyMap = new HashMap();
            int policyDetectCount = mwConfigManageTableDao.countPolicyDetect(detectReport.getReportUUID(), policyManage.getId());
            policyMap.put("typeName", policyManage.getPolicyName());
            policyMap.put("typeId", DetectType.POLICY.getDetectName() + "-" + policyManage.getId());
            policyMap.put("policyDetectCount", policyDetectCount);
            //获取所有的规则ID
            List<Integer> ruleIdList = mwConfigManageTableDao.listDetectRelation(policyManage.getId(), DetectType.POLICY.getDetectName());
            if (CollectionUtils.isEmpty(ruleIdList)){
                continue;
            }
            //获取所有的规则数据
            List<RuleManage> ruleList = mwConfigManageTableDao.listRuleManage(ruleIdList);
            List<Map> childs = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(ruleList)) {
                for (RuleManage ruleManage : ruleList) {
                    Map ruleMap = new HashMap();
                    int ruleDetectCount = mwConfigManageTableDao.countRuleDetect(detectReport.getReportUUID(), ruleManage.getId());
                    ruleMap.put("typeName", ruleManage.getRuleName());
                    ruleMap.put("typeId", DetectType.RULE.getDetectName() + "-" + ruleManage.getId());
                    ruleMap.put("ruleDetectCount", ruleDetectCount);
                    childs.add(ruleMap);
                    allRuleIdList.add(ruleManage.getId());
                }
                policyMap.put("childs", childs);
            }
            typeList.add(policyMap);
        }
    }

    /**
     * 填充资产未扫描数据（该操作为辅助前端展示，无实际意义）
     *
     * @param assetsList 资产数据
     * @param ruleIdList 规则ID列表
     */
    private void reviewAssetsData(List<Map<String, Object>> assetsList, List<Integer> ruleIdList) {
        for (Map assets : assetsList) {
            for (Integer ruleId : ruleIdList) {
                if (assets.containsKey(DetectType.RULE.getDetectName() + "-" + ruleId)) {
                    continue;
                } else {
                    assets.put(DetectType.RULE.getDetectName() + "-" + ruleId, DetectMatchLevel.FAILED.getLevel());
                }
            }
        }
    }

    /**
     * 增加策略管理信息
     *
     * @param policyManageDTO 策略管理
     * @return
     */
    @Override
    @Transactional
    public Reply addPolicyManage(PolicyManageDTO policyManageDTO) {
        if (StringUtils.isEmpty(policyManageDTO.getPolicyName())) {
            return Reply.fail("请填写策略名称");
        }
        if (policyManageDTO.getPolicyTreeGroup() == null || policyManageDTO.getPolicyTreeGroup() == 0) {
            return Reply.fail("请填写策略所在文件夹");
        }
        if (policyManageDTO.getDetectAssetsType() == 0 && ((policyManageDTO.getDetectCondition() == null) ||
                (policyManageDTO.getVendorId() == null || policyManageDTO.getVendorId() == 0))) {
            return Reply.fail("请勾选对应的厂商信息");
        }
        //增加策略信息
        PolicyManage policy = CopyUtils.copy(PolicyManage.class, policyManageDTO);
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        policy.setCreator(userInfo.getLoginName());
        policy.setUpdater(userInfo.getLoginName());
        mwConfigManageTableDao.addPolicyManage(policy);
        policyManageDTO.setId(policy.getId());
        if (CollectionUtils.isNotEmpty(policyManageDTO.getRuleList())){
            //增加策略和规则的对应关系
            List<Integer> list = new ArrayList<>();
            for (String id : policyManageDTO.getRuleList()) {
                list.add(Integer.parseInt(id));
            }
            mwConfigManageTableDao.addDetectRelation(policy.getId(), list, DetectType.POLICY.getDetectName());
        }
        //检测资产类别为自定义检测
        if (policy.getDetectAssetsType() == 1) {
            //增加策略和检测对象的映射关系
            mwConfigManageTableDao.addPolicyAssetsRelation(policy.getId(), policyManageDTO.getAssetsIdList());
        }
        //添加数据权限
        commonService.addMapperAndPerm(policyManageDTO);
        return Reply.ok("增加策略成功");
    }

    /**
     * 更新策略管理数据
     *
     * @param policyManageDTO 策略管理
     * @return
     */
    @Override
    @Transactional
    public Reply updatePolicyManage(PolicyManageDTO policyManageDTO) {
        if (StringUtils.isEmpty(policyManageDTO.getPolicyName())) {
            return Reply.fail("请填写策略名称");
        }
        if (policyManageDTO.getPolicyTreeGroup() == null || policyManageDTO.getPolicyTreeGroup() == 0) {
            return Reply.fail("请填写策略所在文件夹");
        }
        if (policyManageDTO.getDetectAssetsType() == 0 && ((policyManageDTO.getDetectCondition() == null) ||
                (policyManageDTO.getVendorId() == null || policyManageDTO.getVendorId() == 0))) {
            return Reply.fail("请勾选对应的厂商信息");
        }
        //更新策略
        PolicyManage policy = CopyUtils.copy(PolicyManage.class, policyManageDTO);
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        policy.setUpdater(userInfo.getLoginName());
        mwConfigManageTableDao.updatePolicyManage(policy);
        //更新关联关系（先删后增）
        mwConfigManageTableDao.deleteDetectRelation(policy.getId(), DetectType.POLICY.getDetectName());
        //增加检测策略对应关系
        List<Integer> list = new ArrayList<>();
        for (String id : policyManageDTO.getRuleList()) {
            list.add(Integer.parseInt(id));
        }
        if (CollectionUtils.isNotEmpty(list)){
            mwConfigManageTableDao.addDetectRelation(policy.getId(), list, DetectType.POLICY.getDetectName());
        }
        //删除检测对象列表
        mwConfigManageTableDao.deletePolicyAssetsRelation(policy.getId());
        //检测资产类别为自定义检测
        if (policy.getDetectAssetsType() == 1){
            //增加检测对象集合
            mwConfigManageTableDao.addPolicyAssetsRelation(policy.getId(), policyManageDTO.getAssetsIdList());
        }
        //更新策略数据权限
        commonService.updateMapperAndPerm(policyManageDTO);
        return Reply.ok("更新成功！");
    }

    /**
     * 删除策略管理
     *
     * @param policyManageDTO 策略管理
     * @return
     */
    @Override
    @Transactional
    public Reply deletePolicyManage(PolicyManageDTO policyManageDTO) {
        if (CollectionUtils.isEmpty(policyManageDTO.getIds())){
            return Reply.fail("删除列表不能为空!");
        }
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        //批量删除策略数据
        mwConfigManageTableDao.batchDeletePolicyManage(userInfo.getLoginName(),policyManageDTO.getIds());
        //批量删除和规则的关联关系
        mwConfigManageTableDao.batchDeleteDetectRelation(policyManageDTO.getIds(),DetectType.POLICY.getDetectName());
        //批量删除和资产的关联关系
        mwConfigManageTableDao.batchDeletePolicyAssetsRelation(policyManageDTO.getIds());
        //删除数据权限
        List<String> deleteList = new ArrayList<>();
        for (Integer id : policyManageDTO.getIds()) {
            deleteList.add(id + "");
        }
        policyManageDTO.setDeleteIdList(deleteList);
        commonService.deleteMapperAndPerm(policyManageDTO);
        return Reply.ok("删除策略成功");
    }

    /**
     * 获取策略列表
     *
     * @param policyManageDTO 策略管理
     * @return 策略详情列表
     */
    @Override
    public Reply getPolicyList(PolicyManageDTO policyManageDTO) {
        //根据文件夹ID获取
        try {
            //获取当前用户可以看到的所有规则ID
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<String> idList = mwUserService.getAllTypeIdList(userInfo, policyManageDTO.getBaseDataType());
            policyManageDTO.setSystemUser(userInfo.isSystemUser());
            policyManageDTO.setFindInSet(Joiner.on(",").join(idList));
            PageHelper.startPage(policyManageDTO.getPageNumber(), policyManageDTO.getPageSize());
            List<PolicyManage> policyList = mwConfigManageTableDao.listPolicyManage(policyManageDTO);
            List<PolicyManageDTO> list = CopyUtils.copyList(PolicyManageDTO.class, policyList);
            PageInfo pageInfo = new PageInfo<>(policyList);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取策略列表失败", e);
            return Reply.fail("获取策略列表失败");
        }
    }

    /**
     * 获取策略详情
     *
     * @param policyManageDTO 策略管理
     * @return 策略详情
     */
    @Override
    public Reply getPolicyDetail(PolicyManageDTO policyManageDTO) {
        //根据ID获取策略数据
        try {
            PolicyManage policy = mwConfigManageTableDao.getPolicyManageById(policyManageDTO.getId());
            PolicyManageDTO policyManage = CopyUtils.copy(PolicyManageDTO.class, policy);
            //获取策略对应的规则
            List<Integer> relationList = mwConfigManageTableDao.listDetectRelation(policyManage.getId(), DetectType.POLICY.getDetectName());
            List<String> ruleList = new ArrayList<>();
            for (int ruleId : relationList) {
                ruleList.add(DetectType.RULE.getDetectName() + "-" + ruleId);
            }
            policyManage.setRuleList(ruleList);
            //检测资产类别为自定义检测
            if (policy.getDetectAssetsType() == 1) {
                //获取对应的资产数据
                List<String> assetsIdList = mwConfigManageTableDao.listPolicyAssetsRelation(policyManage.getId());
                if (CollectionUtils.isNotEmpty(assetsIdList)) {
                    List<HashMap<String, String>> assetsList = mwConfigManageTableDao.listAssets(assetsIdList);
                    policyManage.setAssetsList(assetsList);
                }
                policyManage.setAssetsIdList(assetsIdList);
            }
            //获取数据权限
            cn.mw.monitor.bean.DataPermission permission = commonService.getDataPermission(policyManage);
            if (permission != null) {
                policyManage.setPrincipal(permission.getUserIds());
                policyManage.setOrgIds(permission.getOrgNodes());
                policyManage.setGroupIds(permission.getGroupIds());
            }
            return Reply.ok(policyManage);
        } catch (Exception e) {
            log.error("获取策略列表失败", e);
            return Reply.fail("获取策略列表失败");
        }
    }

    /**
     * 增加规则管理信息
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    @Override
    @Transactional
    public Reply addRuleManage(MwConfigManageRuleManage ruleManage) {
        if (StringUtils.isEmpty(ruleManage.getRuleName())) {
            return Reply.fail("请填写规则名称");
        }
        if (ruleManage.getRuleTreeGroup() == null || ruleManage.getRuleTreeGroup() == 0) {
            return Reply.fail("请填写规则所在文件夹");
        }
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        //先增加规则管理数据
        RuleManage rule = CopyUtils.copy(RuleManage.class,ruleManage);
        rule.setCreator(userInfo.getLoginName());
        rule.setModifier(userInfo.getLoginName());
        mwConfigManageTableDao.addRuleManage(rule);
        ruleManage.setId(rule.getId());
        //如果存在高级匹配条件，则添加高级匹配规则
        if (ruleManage.getSeniorType() == 1 && CollectionUtils.isNotEmpty(ruleManage.getSeniorMatchRuleList())){
            insertRuleSelect(ruleManage.getSeniorMatchRuleList(),rule.getId());
        }
        //添加数据权限
        commonService.addMapperAndPerm(ruleManage);
        return Reply.ok("添加规则成功！");
    }

    /**
     * 更新规则管理数据
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    @Override
    @Transactional
    public Reply updateRuleManage(MwConfigManageRuleManage ruleManage) {
        if (StringUtils.isEmpty(ruleManage.getRuleName())) {
            return Reply.fail("请填写规则名称");
        }
        if (ruleManage.getRuleTreeGroup() == null || ruleManage.getRuleTreeGroup() == 0) {
            return Reply.fail("请填写规则所在文件夹");
        }
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        //更新规则数据
        RuleManage rule = CopyUtils.copy(RuleManage.class,ruleManage);
        rule.setModifier(userInfo.getLoginName());
        mwConfigManageTableDao.updateRuleManage(rule);
        //先删除高级匹配规则数据
        mwConfigManageTableDao.deleteMwAlertRuleSelect(DetectType.RULE.getDetectName() + "-" +rule.getId());
        //添加高级怕规则数据
        insertRuleSelect(ruleManage.getSeniorMatchRuleList(),ruleManage.getId());
        //修改数据权限
        commonService.updateMapperAndPerm(ruleManage);
        return Reply.ok("更新成功！");
    }

    /**
     * 删除规则管理
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    @Override
    @Transactional
    public Reply deleteRuleManage(MwConfigManageRuleManage ruleManage) {
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        if (CollectionUtils.isEmpty(ruleManage.getIds())) {
            return Reply.fail("删除列表为空！");
        }
        List<String> uuidList = new ArrayList<>();
        List<String> deleteIdList = new ArrayList<>();
        for (int id : ruleManage.getIds()) {
            uuidList.add(DetectType.RULE.getDetectName() + "-" + id);
            deleteIdList.add(id + "");
        }
        //批量删除
        mwConfigManageTableDao.batchDeleteRuleManage(userInfo.getLoginName(), ruleManage.getIds());
        mwConfigManageTableDao.batchDeleteMwAlertRuleSelect(uuidList);
        //删除数据权限
        ruleManage.setDeleteIdList(deleteIdList);
        commonService.deleteMapperAndPerm(ruleManage);
        return Reply.ok("删除规则成功！");
    }

    /**
     * 根据类别获取文件夹及其内部数据
     *
     * @param type 类别
     * @return
     */
    @Override
    public Reply getTreeAndData(String type) {
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        List<String> idList;
        List<TreeData> resultList = new ArrayList<>();
        //获取当前类别的所有文件夹
        List<MwConfigManageTreeGroup> treeGroupList = (List<MwConfigManageTreeGroup>) getTreeGroup(type).getData();
        List<PolicyManage> policyList = new ArrayList<>();
        List<MwConfigManageRuleManage> ruleList = new ArrayList<>();
        //获取当前类别所有数据
        switch (DetectType.getTypeByName(type)) {
            case POLICY:
                idList = mwUserService.getAllTypeIdList(userInfo, DataType.DETECT_POLICY_MANAGE);
                PolicyManageDTO policy = new PolicyManageDTO();
                policy.setSystemUser(userInfo.isSystemUser());
                policy.setFindInSet(Joiner.on(",").join(idList));
                policyList = mwConfigManageTableDao.listPolicyManage(policy);
                break;
            case RULE:
                Map map = new HashMap();
                idList = mwUserService.getAllTypeIdList(userInfo, DataType.DETECT_RULE_MANAGE);
                map.put("isSystem", userInfo.isSystemUser());
                map.put("listSet", Joiner.on(",").join(idList));
                ruleList = mwConfigManageTableDao.getRuleList(map);
                break;
            default:
                return Reply.fail("查询失败");
        }
        if (CollectionUtils.isNotEmpty(policyList)) {
            resultList = handleTreeData(treeGroupList, policyList);
        }
        if (CollectionUtils.isNotEmpty(ruleList)) {
            resultList = handleTreeData(treeGroupList, ruleList);
        }
        return Reply.ok(resultList);
    }

    /**
     * 根据类别获取模糊查询数据
     *
     * @param type 类别
     * @return
     */
    @Override
    public Reply getFuzzList(String type) {
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        List<String> idList;
        List<Map<String, String>> maps = new ArrayList<>();
        try {
            switch (DetectType.getTypeByName(type)) {
                case REPORT:
                    idList = mwUserService.getAllTypeIdList(userInfo, DataType.DETECT_REPORT_MANAGE);
                    maps = mwConfigManageTableDao.fuzzSearchReportData(userInfo.isSystemUser(), Joiner.on(",").join(idList));
                    break;
                case POLICY:
                    idList = mwUserService.getAllTypeIdList(userInfo, DataType.DETECT_POLICY_MANAGE);
                    maps = mwConfigManageTableDao.fuzzSearchPolicyData(userInfo.isSystemUser(), Joiner.on(",").join(idList));
                    break;
                case RULE:
                    idList = mwUserService.getAllTypeIdList(userInfo, DataType.DETECT_RULE_MANAGE);
                    maps = mwConfigManageTableDao.fuzzSearchRuleData(userInfo.isSystemUser(), Joiner.on(",").join(idList));
                    break;
                default:
                    return Reply.fail("查询失败");
            }
            Map<String, List> listMap = new HashMap<>();
            for (Map<String, String> map : maps) {
                if (listMap.get(map.get("type")) == null) {
                    List<String> strings = new ArrayList<>();
                    strings.add(map.get("keyName"));
                    listMap.put(map.get("type"), strings);
                } else {
                    List<String> strings = listMap.get(map.get("type"));
                    strings.add(map.get("keyName"));
                    listMap.put(map.get("type"), strings);
                }
            }
            return Reply.ok(listMap);
        } catch (Exception e) {
            log.error("查询模糊数据列表", e);
            return Reply.fail("查询失败");
        }
    }

    /**
     * 执行合约检测
     *
     * @param reportId 报告ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execAssetsDetect(int reportId) {
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        String uuid = UUIDUtils.getUUID();
        //获取报告数据
        DetectReport report = mwConfigManageTableDao.getDetectReportById(reportId);
        if (report == null) {
            log.error("【执行合约检测】报告数据为空,reportId = " + reportId);
            return;
        }
        //获取报告的策略列表
        List<Integer> policyIdList = mwConfigManageTableDao.listDetectRelation(reportId, DetectType.REPORT.getDetectName());
        if (CollectionUtils.isEmpty(policyIdList)) {
            log.error("【执行合约检测】报告未绑定策略,reportId = " + reportId);
            return;
        }
        //更新最新的UUID
        report.setReportUUID(uuid);
        report.setUpdater(userInfo.getLoginName());
        mwConfigManageTableDao.updateDetectReportUUID(report);
        //查询策略ID列表
        PolicyManageDTO policyManageDTO = new PolicyManageDTO();
        policyManageDTO.setIds(policyIdList);
        List<PolicyManage> policyList = mwConfigManageTableDao.listPolicyManage(policyManageDTO);
        //根据策略列表获取规则数据
        for (PolicyManage policy : policyList) {
            //获取资产ID列表
            List<String> assetsIdList = new ArrayList<>();
            //如果检测资产类别为厂商类别
            if (policy.getDetectAssetsType() == 0) {
                assetsIdList = mwConfigManageTableDao.listPolicyAssetsByVendor(policy.getVendorId(), policy.getDetectCondition());
                //检测类别为自定义
            } else {
                assetsIdList = mwConfigManageTableDao.listPolicyAssetsRelation(policy.getId());
            }
            if (CollectionUtils.isEmpty(assetsIdList)) {
                log.error("【执行合约检测】策略未绑定资产数据" + JSON.toJSONString(policy));
                continue;
            }
            //获取规则ID列表
            List<Integer> ruleIdList = mwConfigManageTableDao.listDetectRelation(policy.getId(), DetectType.POLICY.getDetectName());
            if (CollectionUtils.isEmpty(ruleIdList)) {
                log.error("【执行合约检测】策略未绑定规则数据" + JSON.toJSONString(policy));
                continue;
            }
            //获取规则列表
            List<RuleManage> ruleList = mwConfigManageTableDao.listRuleManage(ruleIdList);
            //增加数据至数据库，同时增加执行线程
            for (String assetsId : assetsIdList) {
                for (RuleManage rule : ruleList) {
                    //保存数据至数据库
                    DetectExecLog execLog = new DetectExecLog();
                    execLog.setReportUUID(uuid);
                    execLog.setAssetsId(assetsId);
                    execLog.setRuleId(rule.getId());
                    execLog.setRuleLevel(rule.getRuleLevel());
                    execLog.setCreator(userInfo.getLoginName());
                    execLog.setUpdater(userInfo.getLoginName());
                    execLog.setReportId(reportId);
                    execLog.setPolicyId(policy.getId());
                    //插入到数据库
                    mwConfigManageTableDao.insertDetectExecLog(execLog);
                    //执行检索任务
                    DetectMatchThread thread = new DetectMatchThread(mwConfigManageTableDao, assetsId,
                            execLog.getId(), rule, ConfigType.getByNumber(policy.getConfigType()));
                    executorService.execute(thread);
                }
            }
        }
    }

    /**
     * 根据策略ID获取关联的报告列表和规则列表
     *
     * @param map 数据
     * @return
     */
    @Override
    public Reply getPolicyRelationList(HashMap map) {
        GlobalUserInfo userInfo = mwUserService.getGlobalUser();
        List<String> idList;
        int policyId = Integer.parseInt(String.valueOf(map.get("id")));
        String type = String.valueOf(map.get("type"));
        int pageNumber = 1;
        int pageSize = 20;
        if (null != map.get("pageNumber")) {
            pageNumber = Integer.parseInt(String.valueOf(map.get("pageNumber")));
        }
        if (null != map.get("pageSize")) {
            pageSize = Integer.parseInt(String.valueOf(map.get("pageSize")));
        }
        List<Map<String, String>> resultList;
        PageHelper.startPage(pageNumber, pageSize);
        switch (DetectType.getTypeByName(type)) {
            case REPORT:
                idList = mwUserService.getAllTypeIdList(userInfo, DataType.DETECT_REPORT_MANAGE);
                resultList = mwConfigManageTableDao.getReportRelationList(policyId,
                        userInfo.isSystemUser(),
                        Joiner.on(",").join(idList));
                break;
            case RULE:
                idList = mwUserService.getAllTypeIdList(userInfo, DataType.DETECT_RULE_MANAGE);
                resultList = mwConfigManageTableDao.getRuleRelationList(policyId,
                        userInfo.isSystemUser(),
                        Joiner.on(",").join(idList));
                break;
            default:
                return Reply.fail("获取失败");
        }
        PageInfo pageInfo = new PageInfo<>(resultList);
        pageInfo.setList(resultList);
        return Reply.ok(pageInfo);
    }

    /**
     * 下载检测报告
     *
     * @param detectReport 检测报告参数
     * @param response     返回数据
     */
    @Override
    public void downloadDetectReport(DetectReportDTO detectReport, HttpServletResponse response) {
        //资产列表
        List<Map<String, Object>> assetsList = new ArrayList<>();
        //栏目列表
        List<Map> typeList = new ArrayList<>();
        try {
            //获取当前报告是否存在UUID
            DetectReport report = mwConfigManageTableDao.getDetectReportById(detectReport.getId());
            if (report == null || StringUtils.isEmpty(report.getReportUUID())) {
                //返回空数据
                updateResponse(response, "报告未执行");
                return;
            }
            //redis存放的KEY
            String redisKey = DETECT_REDIS_PREFIX + report.getReportUUID();
            String typeRedisKey = DETECT_TYPE_REDIS_PREFIX + report.getReportUUID();
            //获取当前报告是否存在未完成的数据
            int allSize = mwConfigManageTableDao.countReportExec(report.getReportUUID());
            int finishedSize = mwConfigManageTableDao.countFinishedReportExec(report.getReportUUID());
            if (allSize != finishedSize) {
                ResponseBase responseBase = new ResponseBase(Constants.HTTP_RES_CODE_500,
                        "报告未完成，当前检测进度为" + finishedSize + "/" + allSize, null);
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().println(JSON.toJSONString(responseBase));
                return;
            }
            //若存在缓存，则直接从缓存取
            if (redisTemplate.hasKey(redisKey)) {
                assetsList = (List<Map<String, Object>>) redisTemplate.opsForValue().get(redisKey);
                typeList = (List<Map>) redisTemplate.opsForValue().get(typeRedisKey);
            } else {
                //获取执行记录列表
                List<DetectExecLog> logList = mwConfigManageTableDao.listLogByUUID(report.getReportUUID());
                List<Integer> allRuleIdList = new ArrayList<>();
                getReportTypeList(report, typeList, allRuleIdList);
                Map<String, Integer> assetsIndexMap = new HashMap<>();
                //获取所有的资产数据
                assetsList = mwConfigManageTableDao.listAssetsByReportUUID(report.getReportUUID());
                int index = 0;
                for (Map assets : assetsList) {
                    assetsIndexMap.put(assets.get("assetsId").toString(), index);
                    index++;
                }
                for (DetectExecLog execLog : logList) {
                    if (!assetsIndexMap.containsKey(execLog.getAssetsId())) {
                        continue;
                    }
                    Map assets = assetsList.get(assetsIndexMap.get(execLog.getAssetsId()));
                    assets.put(DetectType.RULE.getDetectName() + "-" + execLog.getRuleId(), execLog.getRuleLevel());
                }
                //校验是否存在未添加的数据
                reviewAssetsData(assetsList, allRuleIdList);
            }
            response.setHeader("Content-disposition", "attachment; filename=detectReport.xlsx");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
//            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            response.setCharacterEncoding("utf-8");
//            String fileName = URLEncoder.encode(report.getReportName(), "UTF-8").replaceAll("\\+", "%20");
//            response.setHeader("Content-disposition", "attachment;filename*=utf-8''"+fileName+".xlsx");
            ExcelWriterBuilder builder = EasyExcel.write(response.getOutputStream());
            List<List<Object>> dataList = new ArrayList<>();
            List<List<String>> heads = getHeader(typeList, assetsList, dataList);
            builder.head(heads).sheet("报告数据").doWrite(dataList);
        } catch (Exception e) {
            log.error("下载报告出错", e);
            try {
                updateResponse(response, "报告下载失败");
            } catch (IOException xex) {
                log.error("下载报告出错", e);
            }
            return;
        }

    }

    /**
     * 定时任务————下载配置数据
     *
     * @param id 资产ID
     * @return
     */
    @Override
    public TimeTaskRresult downloadConfig(String id) {
        TimeTaskRresult result = new TimeTaskRresult();
        List<PerformResultEntity> results = new ArrayList<>();
        //获取资产数据
        MwTangibleassetsTable assetsInfo = mwConfigManageTableDao.getAssetsById(id);
        if (assetsInfo == null || !assetsInfo.getSettingFlag()) {
            return result.setFailReason("资产未查到或未选中配置选项");
        }
        for (ConfigType configType : ConfigType.values()) {
            if (configType == ConfigType.ALL) {
                continue;
            }
            assetsInfo.setId(id);
            assetsInfo.setAssetsId(id);
            MwDownloadService mwDownloadService = new MwDownloadService(assetsInfo, iLoginCacheInfo,
                    mwConfigManageTableDao, mwPerfromService, false, configType.getDataBaseValue(),commonService);
            PerformResultEntity res = mwDownloadService.call();
            results.add(res);
        }
        //判断返回结果
        for (PerformResultEntity f : results) {
            PerformResultEntity resultEntity = f;
            if (resultEntity.getIsSuccess()) {
            } else {
                return result.setFailReason("下载配置执行失败！");
            }
        }
        StringBuffer configUrl = new StringBuffer();
        for (ConfigType configType : ConfigType.values()) {
            if (configType == ConfigType.ALL) {
                continue;
            }
        }
        return result.setSuccess(true).setResultType(1).setResultContext(results.get(0).getPath());
    }



    /**
     * 定时任务————自动tftp上传
     *
     * @param id 资产ID
     * @return
     */
    public TimeTaskRresult downloadTFTPConfig(String id) {
        TimeTaskRresult result = new TimeTaskRresult();
        List<PerformResultEntity> results = new ArrayList<>();
        //获取资产数据
        MwTangibleassetsTable assetsInfo = mwConfigManageTableDao.getAssetsById(id);
        if (assetsInfo == null || !assetsInfo.getSettingFlag()) {
            return result.setFailReason("资产未查到或未选中配置选项");
        }

            assetsInfo.setId(id);
            assetsInfo.setAssetsId(id);
            MwDownloadService mwDownloadService = new MwDownloadService(assetsInfo, iLoginCacheInfo,
                    mwConfigManageTableDao, mwPerfromService, false, CONFIG_TFTP,commonService);
            PerformResultEntity res = mwDownloadService.call();
            results.add(res);

        //判断返回结果
        for (PerformResultEntity f : results) {
            PerformResultEntity resultEntity = f;
            if (resultEntity.getIsSuccess()) {
            } else {
                return result.setFailReason("下载配置执行失败！");
            }
        }
        StringBuffer configUrl = new StringBuffer();
        for (ConfigType configType : ConfigType.values()) {
            if (configType == ConfigType.ALL) {
                continue;
            }
        }
        return result.setSuccess(true).setResultType(1).setResultContext(results.get(0).getPath());
    }




    /**
     * 定时任务————执行配置脚本
     *
     * @param id      资产ID
     * @param command 执行命令
     * @return
     */
    @Override
    public TimeTaskRresult execConfigScript(String id, String command) {
        TimeTaskRresult result = new TimeTaskRresult();
        List<PerformResultEntity> results = new ArrayList<>();
        //获取资产数据
        MwTangibleassetsTable assetsInfo = mwConfigManageTableDao.getAssetsById(id);
        if (assetsInfo == null || !assetsInfo.getSettingFlag()) {
            return result.setFailReason("资产未查到或未选中配置选项");
        }
        assetsInfo.setId(id);
        assetsInfo.setAssetsId(id);
        MwPerformService mwPerformService = new MwPerformService(null, assetsInfo, iLoginCacheInfo,
                mwConfigManageTableDao, mwPerfromService, false, command);
        PerformResultEntity res = mwPerformService.call();
        results.add(res);
        //判断返回结果
        for (PerformResultEntity f : results) {
            PerformResultEntity resultEntity = f;
            if (resultEntity.getIsSuccess()) {
            } else {
                return result.setFailReason("执行命令失败！");
            }
        }
        return result.setSuccess(true).setResultType(1).setResultContext(res.getPath());
    }

    /**
     * 定时任务————比较当前文件的最近时间两个配置文件差异性
     *
     * @param id 资产ID
     * @return
     */
    @Override
    public TimeTaskRresult compareConfigContent(String id) {
        TimeTaskRresult result = new TimeTaskRresult();
        Map map = new HashMap();
        map.put("assetsId", id);
        for (ConfigType configType : ConfigType.values()) {
            if (configType == ConfigType.ALL) {
                continue;
            }
            map.put("configType", configType.getDataBaseValue());
            List<MwNcmDownloadConfig> data = mwConfigManageTableDao.selectDownloads(map);
            if (CollectionUtils.isEmpty(data) || data.size() <= 1) {
                return result.setSuccess(true).setResultType(0);
            }
            MwNcmDownloadConfig newConfig = data.get(0);
            MwNcmDownloadConfig oldConfig = data.get(1);
            try {
                String oldFilePath = oldConfig.getPath() + "/" + oldConfig.getName();
                String newFilePath = newConfig.getPath() + "/" + newConfig.getName();
                //文件比较
                List<String> original = Files.readAllLines(new File(oldFilePath).toPath());
                List<String> revised = Files.readAllLines(new File(newFilePath).toPath());
                if (CollectionUtils.isEmpty(original) || CollectionUtils.isEmpty(revised)) {
                    return result;
                }
                Patch<String> patch = DiffUtils.diff(original, revised);
                if (CollectionUtils.isEmpty(patch.getDeltas())) {
//                    //比对无差异，删除以前的差异化数据
//                    mwConfigManageTableDao.deleteConfigChange(id);
                } else {
//                    //存在差异，删除已存在的差异数据，再重新保存
//                    mwConfigManageTableDao.deleteConfigChange(id);
                    mwConfigManageTableDao.insertConfigChange(id, oldFilePath, newFilePath);
                    // TODO: 2022/3/17 增加邮箱推送功能
                }
            } catch (Exception e) {
                log.error("比对文本失败", e);
                return result;
            }
        }
        return result.setSuccess(true).setResultType(0).setResultContext("执行成功");
    }

    /**
     * 执行合规检测报告
     *
     * @param id 报告ID
     * @return
     */
    @Override
    public TimeTaskRresult execReport(String id) {
        TimeTaskRresult result = new TimeTaskRresult();
        int reportId = Integer.parseInt(id);
        execAssetsDetect(reportId);
        return result.setSuccess(true).setResultType(0).setResultContext("执行成功");
    }

    /**
     * 获取配置信息变化的资产数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply selectChangedConfigs(MwNcmDownloadConfig param) {
        List<Map> resultList = mwConfigManageTableDao.selectChangedConfigs(param.getId());
        PageInfo pageInfo = new PageInfo<>(resultList);
        pageInfo.setList(resultList);
        return Reply.ok(pageInfo);
    }

    /**
     * 定时清除资产配置数据
     *
     * @param assetsId 资产ID
     */
    @Override
    public void clearConfigSetting(String assetsId) {
        try {
            //是否根据数量删除数据
            boolean countFlag = false;
            //是否根据时间删除数据
            boolean timeFlag = false;
            //获取保留时间和数量配置
            MwNcmPath path = mwConfigManageTableDao.getPath();
            if (path.getMaxCount() != null && path.getMaxCount() > 0 && path.checkMaxCount()) {
                countFlag = true;
            }
            if (path.getMaxTime() != null && path.getMaxTime() > 0 && path.checkMaxTime()) {
                timeFlag = true;
            }
            List<MwNcmDownloadConfig> delConfigList = new ArrayList<>();
            List<MwNcmDownloadConfig> delPerformList = new ArrayList<>();
            //取出当前资产的所有数据
            for (ConfigType configType : ConfigType.values()) {
                if (configType == ConfigType.ALL) {
                    continue;
                }
                Map map = new HashMap();
                map.put("assetsId", assetsId);
                map.put("configType", configType.getDataBaseValue());
                List<MwNcmDownloadConfig> downloadList = mwConfigManageTableDao.selectDownloads(map);
                if (countFlag) {
                    if (downloadList != null && downloadList.size() > path.getMaxCount()) {
                        delConfigList.addAll(downloadList.subList(path.getMaxCount(), downloadList.size()));
                    }
                }
                if (timeFlag) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.DATE, -path.getMaxTime());
                    for (MwNcmDownloadConfig config : downloadList) {
                        if (calendar.getTime().after(config.getCreateDate())) {
                            delConfigList.add(config);
                        }
                    }
                }
            }
            Map map = new HashMap();
            map.put("assetsId", assetsId);
            List<MwNcmDownloadConfig> downloadList = mwConfigManageTableDao.selectPerforms(map);
            if (countFlag) {
                if (downloadList != null && downloadList.size() > path.getMaxCount()) {
                    delPerformList.addAll(downloadList.subList(path.getMaxCount(), downloadList.size()));
                }
            }
            if (timeFlag) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, -path.getMaxTime());
                for (MwNcmDownloadConfig config : downloadList) {
                    if (calendar.getTime().after(config.getCreateDate())) {
                        delPerformList.add(config);
                    }
                }
            }
            //删除文件+删除数据库
            deleteConfigs(delConfigList);
            deletePerforms(delPerformList);
        } catch (Exception e) {
            log.error("删除配置文件和执行文件失败", e);
        }
    }

    /**
     * 批量下载执行文件
     *
     * @param map      参数
     * @param response 返回内容
     */
    @Override
    public void batchDownloadPerform(HashMap map, HttpServletResponse response) {
        ZipArchiveOutputStream zous = null;
        try {
            String createTimeString  = (String) map.get("createTimeStart");
            String endTimeString  = (String) map.get("createTimeEnd");
            Date createTimeStart = DateUtils.parse(createTimeString);
            Date createTimeEnd = DateUtils.parse(endTimeString);

            Calendar createTimeCalendar = Calendar.getInstance();
            createTimeCalendar.setTime(createTimeStart);
            createTimeCalendar.set(Calendar.HOUR_OF_DAY,0);
            createTimeCalendar.set(Calendar.MINUTE,0);
            createTimeCalendar.set(Calendar.SECOND,0);

            Calendar endTimeCalendar = Calendar.getInstance();
            endTimeCalendar.setTime(createTimeEnd);
            endTimeCalendar.set(Calendar.HOUR_OF_DAY,23);
            endTimeCalendar.set(Calendar.MINUTE,59);
            endTimeCalendar.set(Calendar.SECOND,59);
            //获取带下载的资产配置数据
            List<MwNcmDownloadConfig> downloadConfigList = mwConfigManageTableDao.selectDownloadInfos(createTimeCalendar.getTime(),
                    endTimeCalendar.getTime());

            if (map.containsKey("requestType") && "json".equals(map.get("requestType"))) {
                if (createTimeStart == null || createTimeEnd == null || createTimeStart.after(createTimeEnd)) {
                    updateResponse(response, "下载失败,时间参数不正确");
                    return;
                }
                if (CollectionUtils.isEmpty(downloadConfigList)) {
                    updateResponse(response, "下载失败,无数据");
                    return;
                }
                ResponseBase responseBase = new ResponseBase(Constants.HTTP_RES_CODE_200, "成功", Reply.ok());
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().println(JSON.toJSONString(responseBase));
                return;
            }
            //设置响应
            response.reset();
            response.setContentType("application/octet-stream");
            response.setHeader("Accept-Ranges", "bytes");

            String fileName = URLEncoder.encode("资产配置备份_" + DateUtils.nowDate() + ".zip", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            //参数组装
            zous = new ZipArchiveOutputStream(response.getOutputStream());
            zous.setUseZip64(Zip64Mode.AsNeeded);
            zous.setEncoding("UTF-8");

            String resultString = null;
            String path = uploadFolder+File.separator+"tftp";
            for (MwNcmDownloadConfig config : downloadConfigList) {
                if (config.getConfigType().equals("TFTP")){
                    zous = new ZipArchiveOutputStream(response.getOutputStream());
                    zous.setUseZip64(Zip64Mode.AsNeeded);
                    zous.setEncoding("UTF-8");

                    ArchiveEntry entry = new ZipArchiveEntry(config.getContext() + "_" + DateUtils.nowDate() +
                            File.separator + config.getName().replaceAll(".mwcfg", ".cfg"));
                    zous.putArchiveEntry(entry);
                    resultString = showUnPassword(path+ Matcher.quoteReplacement(File.separator)+ config.getName().replaceAll(".mwcfg", ".cfg"));
                    zous.write(resultString.getBytes("UTF-8"));
                    zous.closeArchiveEntry();
                    zous.flush();
                }
                    ArchiveEntry entry = new ZipArchiveEntry(config.getContext() + "_" + DateUtils.nowDate() +
                            File.separator + config.getName().replaceAll(".mwcfg", ".config"));
                    zous.putArchiveEntry(entry);
                    resultString = showTxt(config.getPath() + "/" + config.getName());
                    zous.write(resultString.getBytes("UTF-8"));
                    zous.closeArchiveEntry();
                    zous.flush();
                }
            zous.closeArchiveEntry();
        } catch (Exception e) {
            try {
                updateResponse(response, "下载失败");
            } catch (IOException ex) {
                log.error("批量下载执行文件失败", ex);
            }
            log.error("批量下载执行文件失败", e);
        } finally {
            if (zous != null) {
                try {
                    zous.close();
                } catch (IOException e) {
                    log.error("批量下载执行文件失败", e);
                }
            }
        }
    }

    private String showUnPassword(String filename) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        String str = "";
        try {
            br = new BufferedReader(new FileReader(filename));
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(System.lineSeparator() + s);
            }

            String result = sb.toString();
            str = result;
        /*    str = EncryptUtil.decrypt(result);*/
        } catch (Exception e) {
            log.error("查看文本失败",e);
            return e.getMessage();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error("查看文本失败",e);
            }
        }

        return str;
    }

    /**
     * 获取配置管理模糊查询字段
     *
     * @param type 查询类别
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData(String type) {
        List<Map<String, String>> maps  = new ArrayList<>();
        switch (type){
            case "configManage":
                maps = mwConfigManageTableDao.fuzzSearchAssetsData();
                break;
            case "accountManage":
                maps = mwConfigManageTableDao.fuzzSearchAccountData();
                break;
            case "templateManage":
                maps = mwConfigManageTableDao.fuzzSearchTemplateData();
                break;
        }
        Map<String, List> listMap = new HashMap<>();
        Set<String> allSet = new HashSet<>();
        String keyName;
        for (Map<String, String> map : maps) {
            keyName = map.get("keyName");
            if (StringUtils.isEmpty(keyName)) {
                continue;
            }
            if (listMap.get(map.get("type")) == null) {
                List<String> strings = new ArrayList<>();
                strings.add(keyName);
                listMap.put(map.get("type"), strings);
            } else {
                List<String> strings = listMap.get(map.get("type"));
                strings.add(keyName);
                listMap.put(map.get("type"), strings);
            }
            allSet.add(keyName);
        }
        List<String> fuzzyQuerys = new ArrayList<>();
        fuzzyQuerys.addAll(allSet);
        Collections.sort(fuzzyQuerys);
        listMap.put("fuzzyQuery", fuzzyQuerys);
        return Reply.ok(listMap);
    }

    /**
     * 导出excel模板
     *
     * @param response 导出数据
     */
    @Override
    public void excelTemplateExport(HttpServletResponse response) {
        //获取所有资产配置数据
        List<ExportAssetsParam> list = mwConfigManageTableDao.listAssetsExport();
        //插入导入项
        Set<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("assetsName");
        includeColumnFiledNames.add("templateName");
        includeColumnFiledNames.add("accountName");
        includeColumnFiledNames.add("userName");
        includeColumnFiledNames.add("password");
        includeColumnFiledNames.add("protocol");
        includeColumnFiledNames.add("port");
        includeColumnFiledNames.add("assetsId");
        ExcelWriter excelWriter = null;
        try {
            excelWriter = ExcelUtils.getExcelWriter("exportAssetsTemplate", response, ExportAssetsParam.class);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(list, sheet);
            log.info("导出成功");
        } catch (IOException e) {
            log.error("导出失败", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 用户批量导入
     *
     * @param file     excel文件数据
     * @param response 失败数据返回
     */
    @Override
    public void excelImport(MultipartFile file, HttpServletResponse response) {
        try {
            String fileName = file.getOriginalFilename();
            String loginName = iLoginCacheInfo.getLoginName();
            if (null != fileName && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                EasyExcel.read(file.getInputStream(), ExportAssetsParam.class,
                        new AssetsExcelImportListener(response, fileName, loginName)).sheet().doRead();
            } else {
                log.error("没有传入正确的excel文件名", file);
            }
        } catch (Exception e) {
            log.error("fail to excelImport with MultipartFile={}, cause:{}", file, e);
        }
    }

    /**
     * 用户批量导入
     *
     * @param file     excel文件数据
     * @param response 失败数据返回
     */
    @Override
    public void excelImport(File file, HttpServletResponse response) {
        try {
            String fileName = "exportAssetsTemplate.xlsx";
            String loginName = iLoginCacheInfo.getLoginName();
            FileInputStream inputStream = new FileInputStream(file
            );
            if (null != fileName && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                EasyExcel.read(inputStream, ExportAssetsParam.class,
                        new AssetsExcelImportListener(response, fileName, loginName)).sheet().doRead();
            } else {
                log.error("没有传入正确的excel文件名", file);
            }
        } catch (Exception e) {
            log.error("fail to excelImport with MultipartFile={}, cause:{}", file, e);
        }
    }

    @Override
    public Reply configManagebrowselist(QueryTangAssetsParam qParam) {
        PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
        String ipAddress = "";
        List<MwTangibleassetsTable>  mwTangAssetses = new ArrayList<>();
        if (qParam.getFuzzyQuery()!=null&&qParam.getFuzzyQuery().contains(",")){
            List<String> strings = Arrays.asList(qParam.getFuzzyQuery().split(","));
            String  s = strings.get(0);
            String p = strings.get(1);
            mwTangAssetses = mwConfigManageTableDao.selectOutAccountList(s,p);
        }
        else{
            mwTangAssetses = mwConfigManageTableDao.selectOutAccount(qParam.getAccountId()==null?"0":qParam.getAccountId(),qParam.getFuzzyQuery());
        }

        PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
        pageInfo.setList(mwTangAssetses);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply createVariable(List<MwScriptVariable> qParam) {
        List<Integer> ids = new ArrayList<>();
        for (MwScriptVariable mwScriptVariable:qParam) {
            Integer integer = mwConfigManageTableDao.insertVariable(mwScriptVariable);
            ids.add(mwScriptVariable.getId());
        }
        return Reply.ok(ids);
    }

    @Override
    public Reply getVariable(List<MwScriptVariable> qParam) {
        List<Integer> ids = new ArrayList<>();
        List<MwScriptVariable> mwScriptVariable = new ArrayList<>();
        for (MwScriptVariable m:qParam) {
            ids.add(m.getId());
            mwScriptVariable=mwConfigManageTableDao.getVariable(ids);
        }

        return Reply.ok(mwScriptVariable);
    }

    @Override
    public Reply selectAssets(QueryTangAssetsParam qParam) {
        PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
        List<MwTangibleassetsTable>  mwTangAssetses = mwConfigManageTableDao.selectOutAccount(qParam.getAccountId(),qParam.getFuzzyQuery());
        PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
        pageInfo.setList(mwTangAssetses);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply getVariableById(Integer integer) {
        List<MwScriptVariable> mwScriptVariable = mwConfigManageTableDao.getVariableById(integer);
        return Reply.ok(mwScriptVariable);
    }

    @Override
    public Reply browselistAssets(List<QueryTangAssetsParam> qParam) {
        List<String> ids = new ArrayList<>();
        for (QueryTangAssetsParam q:qParam) {
            ids.add(q.getAssetsId());
        }
        List<MwTangibleassetsTable> list = mwConfigManageTableDao.selectOutAssets(ids);
        return Reply.ok(list);
    }

    /**
     * 更新response，返回JSON格式数据
     * @param response
     * @param s
     * @throws IOException
     */
    private void updateResponse(HttpServletResponse response, String s) throws IOException {
        Reply reply = Reply.fail(s);
        ResponseBase responseBase = new ResponseBase(Constants.HTTP_RES_CODE_200, s, reply);
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(JSON.toJSONString(responseBase));
    }


    /**
     * 处理头部信息和数据信息
     *
     * @param typeList   类别信息
     * @param assetsList 待处理的结果信息
     * @param dataList   数据信息
     * @return 头部信息列表
     */
    private List<List<String>> getHeader(List<Map> typeList, List<Map<String, Object>> assetsList, List<List<Object>> dataList) {
        List<List<String>> heads = new ArrayList<>();
        List<String> head0 = new ArrayList<>();
        head0.add("资产名称");
        List<String> head1 = new ArrayList<>();
        head1.add("IP地址");
        heads.add(head0);
        heads.add(head1);
        Map<Integer, String> ruleIndexMap = new HashedMap();
        int index = 0;
        for (Map type : typeList) {
            String firstHeaderName = (String) type.get("typeName");
            List<Map> childs = (List<Map>) type.get("childs");
            if (CollectionUtils.isNotEmpty(childs)) {
                for (Map child : childs) {
                    List<String> typeHead = new ArrayList<>();
                    typeHead.add(firstHeaderName);
                    typeHead.add((String) child.get("typeName"));
                    ruleIndexMap.put(index, (String) child.get("typeId"));
                    heads.add(typeHead);
                    index++;
                }
            }
        }
        for (Map assets : assetsList) {
            List<Object> contentList = new ArrayList<>(ruleIndexMap.keySet().size() + 2);
            contentList.add(assets.get("assetsName"));
            contentList.add(assets.get("inBandIP"));
            for (int ruleIndex : ruleIndexMap.keySet()) {
                String typeId = ruleIndexMap.get(ruleIndex);
                String level = "正常";
                int ruleLevel = (int) assets.get(typeId);
                DetectMatchLevel detectMatchLevel = DetectMatchLevel.getByLevel(ruleLevel);
                switch (detectMatchLevel) {
                    case NROMAL:
                        level = "信息";
                        break;
                    case WARNING:
                        level = "警告";
                        break;
                    case ERROR:
                        level = "严重";
                        break;
                    case FAILED:
                    default:
                        break;
                }
                contentList.add(level);
            }
            dataList.add(contentList);
        }
        return heads;
    }

    /**
     * 将文件夹和数据整合在一起
     * @param groupList 树列表
     * @param dataList 数据列表
     * @return
     */
    private List<TreeData> handleTreeData(List<MwConfigManageTreeGroup> groupList, List<?> dataList) {
        List<TreeData> resultList = new ArrayList<>();
        for (MwConfigManageTreeGroup group : groupList) {
            TreeData treeData = new TreeData();
            treeData.setNode("GROUP-" + group.getId());
            treeData.setNodeName(group.getName());
            treeData.setType("FILE");
            treeData.setParentNode("GROUP-" + group.getParentId());
            //查找当前树节点下的所有数据
            List<TreeData> childList = new ArrayList<>();
            Iterator iterator = dataList.iterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                if (object instanceof PolicyManage) {
                    PolicyManage policyManage = (PolicyManage) object;
                    if (!policyManage.getPolicyTreeGroup().equals(group.getId())) {
                        continue;
                    }
                    TreeData childData = new TreeData();
                    childData.setNode(DetectType.POLICY.getDetectName() + "-" + policyManage.getId());
                    childData.setType("DOC");
                    childData.setParentNode(treeData.getNode());
                    childData.setNodeName(policyManage.getPolicyName());
                    childList.add(childData);
                    iterator.remove();
                } else if (object instanceof MwConfigManageRuleManage) {
                    MwConfigManageRuleManage rule = (MwConfigManageRuleManage) object;
                    if (!group.getId().equals(rule.getRuleTreeGroup())) {
                        continue;
                    }
                    TreeData childData = new TreeData();
                    childData.setNode(DetectType.RULE.getDetectName() + "-" + rule.getId());
                    childData.setType("DOC");
                    childData.setParentNode(treeData.getNode());
                    childData.setNodeName(rule.getRuleName());
                    childList.add(childData);
                    iterator.remove();
                }
            }
            if (CollectionUtils.isNotEmpty(group.getMwConfigManageTreeGroups())) {
                childList.addAll(handleTreeData(group.getMwConfigManageTreeGroups(), dataList));
            }
            treeData.setChildList(childList);
            resultList.add(treeData);
        }
        return resultList;
    }


    /**
     * 获取树结构数据
     *
     * @param list 树列表
     * @param i    节点深度
     * @return
     */
    private List<MwConfigManageTreeGroup> groupTree(List<MwConfigManageTreeGroup> list, Integer i) {
        List<MwConfigManageTreeGroup> child = new ArrayList<>();
        for (MwConfigManageTreeGroup g : list) {
            if (g.getParentId().equals(i)) {
                List<MwConfigManageTreeGroup> lischild = groupTree(list, g.getId());
                g.setMwConfigManageTreeGroups(lischild);
                child.add(g);
            }
        }
        return child;
    }

    /**
     * 批量插入高级匹配规则
     *
     * @param seniorMatchRuleList 高级匹配规则列表
     * @param ruleId              规则ID
     */
    private void insertRuleSelect(List<MwRuleSelectParam> seniorMatchRuleList, int ruleId) {
        if (CollectionUtils.isNotEmpty(seniorMatchRuleList)) {
            List<MwRuleSelectParam> paramList = new ArrayList<>();
            for (MwRuleSelectParam selectParam : seniorMatchRuleList) {
                MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
                ruleSelectDto.setCondition(selectParam.getCondition());
                ruleSelectDto.setDeep(selectParam.getDeep());
                ruleSelectDto.setKey(selectParam.getKey());
                ruleSelectDto.setName(selectParam.getName());
                ruleSelectDto.setParentKey(selectParam.getParentKey());
                ruleSelectDto.setRelation(selectParam.getRelation());
                ruleSelectDto.setValue(selectParam.getValue());
                ruleSelectDto.setUuid(DetectType.RULE.getDetectName() + "-" + ruleId);
                paramList.add(ruleSelectDto);
                if (CollectionUtils.isNotEmpty(selectParam.getConstituentElements())) {
                    paramList.addAll(delMwRuleSelectList(selectParam,ruleId));
                }
            }
            mwConfigManageTableDao.insertMwAlertRuleSelect(paramList);
        }
    }

    /**
     * 处理层级数据
     * @param param 层级数据
     * @return
     */
    private List<MwRuleSelectParam> delMwRuleSelectList(MwRuleSelectParam param,int ruleId){
        List<MwRuleSelectParam> paramList = new ArrayList<>();
        for (MwRuleSelectParam s : param.getConstituentElements()){
            MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
            ruleSelectDto.setCondition(s.getCondition());
            ruleSelectDto.setDeep(s.getDeep());
            ruleSelectDto.setKey(s.getKey());
            ruleSelectDto.setName(s.getName());
            ruleSelectDto.setParentKey(s.getParentKey());
            ruleSelectDto.setRelation(s.getRelation());
            ruleSelectDto.setValue(s.getValue());
            ruleSelectDto.setUuid(DetectType.RULE.getDetectName() + "-" + ruleId);
            paramList.add(ruleSelectDto);
            s.setUuid(param.getUuid());
            if(s.getConstituentElements() != null && s.getConstituentElements().size() > 0){
                List<MwRuleSelectParam> temps = delMwRuleSelectList(s,ruleId);
                paramList.addAll(temps);
            }
        }
        return paramList;

    }

    /**
     * 获取子节点数据
     * @param key  节点
     * @param rootList 根数据
     * @return
     */
    private static List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList){
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for(MwRuleSelectParam s : rootList){
            if(s.getParentKey().equals(key)){
                childList.add(s);
            }
        }
        for(MwRuleSelectParam s : childList){
            s.setConstituentElements(getChild(s.getKey(),rootList));
        }
        if(childList.size() == 0){
            return null;
        }
        return childList;

    }

    /**
     * 开关接口
     *
     * @param assetsId      资产ID
     * @param interfaceName 接口名称
     * @param switchState   开关状态（true:打开接口  false：关闭接口）
     * @return true:下发命令成功    false:下发命令失败
     */
    @Override
    public boolean switchInterface(String assetsId, String interfaceName, boolean switchState) {
        try {
            MwTangibleassetsTable assets = mwConfigManageTableDao.getAssetsById(assetsId);
            if (assets == null || !assets.getSettingFlag()){
                return false;
            }
            assets.setId(assetsId);
            SwitchInterfaceThread switchThread = new SwitchInterfaceThread(assets, mwConfigManageTableDao,
                    mwPerfromService, interfaceName, switchState);
            return switchThread.call();
        } catch (Exception e) {
            log.error("下发开关状态失败", e);
            return false;
        }
    }

    @Override
    public Map<String, String> getAsstetByid(String assetsId) {
        Map<String,String> asstest =  mwConfigManageTableDao.getAsstetByid(assetsId);
        return asstest;
    }


}
