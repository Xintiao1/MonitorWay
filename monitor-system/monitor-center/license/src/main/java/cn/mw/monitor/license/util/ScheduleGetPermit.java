package cn.mw.monitor.license.util;

import cn.mw.monitor.common.constant.ModuleDesc;
import cn.mw.monitor.license.config.MWLicenseConfigLoad;
import cn.mw.monitor.service.license.param.LicenseXmlParam;
import cn.mw.monitor.service.license.service.CheckLicenseService;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.util.HostUtils;
import cn.mw.monitor.util.LicenseEnum;
import cn.mw.monitor.util.ModuleNameEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 定时获取许可文件中的信息
 */

@Component
@Slf4j(topic = "MWLicenseController")
public class ScheduleGetPermit {
    @Autowired
    CheckLicenseService checkLicenseService;
    @Autowired
    private MWMessageService mwMessageService;
    @Value("${mwProduce.days}")
    private  int days;

    @Value("${mwProduce.genSecurity}")
    private boolean genSecurity;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public  int isPermit(String moduleName){
        int permit = getPermit(moduleName);
        if(permit == 0){
            return permit;
        }
        if(moduleName.equals(ModuleNameEnum.ASSETS_MANAGE.toString())){
            int permitServer = getPermit(ModuleNameEnum.ASSETS_MANAGE_SERVER.toString());
            int permitNet = getPermit(ModuleNameEnum.ASSETS_MANAGE_NET.toString());
            int permitStorage = getPermit(ModuleNameEnum.ASSETS_MANAGE_STORAGE.toString());
            int permitWeb = getPermit(ModuleNameEnum.ASSETS_MANAGE_WEB.toString());
            if(permitServer == 0 || permitNet == 0 || permitStorage == 0 || permitWeb == 0){
                return 0;
            }
        }
        return permit;
    }

    public int getPermit(String moduleName){
        try {
            ConcurrentHashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
            LicenseXmlParam param = propMap.get(moduleName);
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            Date parse1 = sf.parse(param.getExpireDate());
            Date date = new Date();
            //卫语句 判断异常
            if(propMap==null){return 1; }
            if(param.getCode()==2){return 1;}
            log.info("sn是多少HostUtils?");
            Date start = new Date();
            String sn = HostUtils.getSn(genSecurity);
            Date end = new Date();
            long internal = end.getTime() - start.getTime();
            log.info("sn是多少HostUtils:{},internal:{}" ,sn ,internal);
            log.info("sn是多少param："  + param.getHostMsg());
            if (!sn.toLowerCase().equals(param.getHostMsg().toLowerCase())) { return 3;}
            if(Long.parseLong(redisTemplate.opsForValue().get(moduleName + "_date")) <= 0 && param.getState().equals(LicenseEnum.subscribed.getChNum())){
                return 7;
            }
            log.info("时间：" + redisTemplate.opsForValue().get(moduleName + "_date"));
            if(Long.parseLong(redisTemplate.opsForValue().get(moduleName + "_date")) <= 0){return 4;}
            //if(param.isStopIs()){return 6;}
            long time = parse1.getTime()- date.getTime();
            long d=time/(3600*24*1000);
            log.info(moduleName+"剩下"+d+"天");
            if(d<days){
                new Thread(()->{
                    checkLicenseService.insertLicenseInfo(moduleName,(int)d);
                }).start();
                if(!param.getState().equals(LicenseEnum.test.getChNum())) return 8;
            }else{
                new Thread(()->{
                    checkLicenseService.deleteLicenseInfo(moduleName);
                }).start();
            }
            return 0;
        }catch (Exception e){
            log.error("许可管理异常：", e);
            return 1;
        }
    }
//    public  int isPermit(String moduleName){
//        try {
//            File file = new File(filePath);
//            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//            Document parse = documentBuilder.parse(file);
//            NodeList moduleKey = parse.getElementsByTagName("permit");
//            for (int i = 0; i <moduleKey.getLength() ; i++) {
//                String hostMsg = parse.getElementsByTagName("hostMsg").item(i).getFirstChild().getNodeValue();
//                String signature = parse.getElementsByTagName("signature").item(i).getFirstChild().getNodeValue();
//                String sign = RSAUtils.decryptData(signature, key);
//                String[] split = sign.split("=");
//                if(split.length==2){
//                    String expireDate = split[0];
//                    String moduleId = split[1];
//                    if(moduleId.equals(moduleName)){
//                        String host = RSAUtils.decryptData(hostMsg, key);
//                        //机器码相同时才判断到期日
//                        if(HostUtils.getSn().equals(host)){
//                            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
//                            Date parse1 = sf.parse(expireDate);
//                            Date date = new Date();
//                            if(parse1.after(date)){
//                                long time = parse1.getTime()- date.getTime();
//                                long d=time/(3600*24*1000);
//                                log.info("剩下"+d+"天");
//                                if(d<days){
//                                        new Thread(()->{
//                                            checkLicenseService.insertLicenseInfo(moduleName,(int)d);
//                                        }).start();
//                                }else{
//                                        new Thread(()->{
//                                                checkLicenseService.deleteLicenseInfo(moduleName);
//                                        }).start();
//                                }
//                                return 0;
//                            }else {
//                                //时间到期
//                                return 4;
//                            }
//                        }else {
//                            //机器码不正确
//                            return 3;
//                        }
//                    }else {
//                        //模块找不到
//                        return 2;
//                    }
//                }
//            }
//        }catch (Exception e){
//            log.info("获取产品许可文件信息失败");
//            return 1;
//        }
//        //许可证书内容有误
//        return 5;
//    }
}
