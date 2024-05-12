package cn.mw.monitor.license.config;

import cn.mw.monitor.common.constant.ModuleDesc;
import cn.mw.monitor.license.dao.MwCheckLicenseDao;
import cn.mw.monitor.license.service.impl.UpdateLicenseProp;
import cn.mw.monitor.service.license.param.LicenseModuleldEnum;
import cn.mw.monitor.service.license.param.LicenseModuleldNonControlEnum;
import cn.mw.monitor.service.license.param.LicenseXmlParam;
import cn.mw.monitor.util.RSAUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class MWLicenseConfigLoad implements ApplicationRunner, UpdateLicenseProp {
    private static final Logger log = LoggerFactory.getLogger("MWLicenseController");
    @Value("${mwProduce.filePath}")
    private String filePath;

    @Value("${file.url}")
    private String imgPath;

    //@Value("${mwProduce.key}")
    private String key;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    MwCheckLicenseDao checkLicenseDao;
    public static ConcurrentHashMap<String, LicenseXmlParam> propMap = new ConcurrentHashMap<>();

    public static String company;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            //updateMwCustomcolTable();
            loadFilePrivateKey();
            loadFile();
            timeDown();
        } catch (Exception e) {
            log.info("许可文件加载失败");
            log.error("license error:", e);
        }
    }

    public void updateMwCustomcolTable(){
        //初始化所有栏目不可见
        checkLicenseDao.updateMwModule(null,true,LicenseModuleldNonControlEnum.NON_CONTROL.getModuleIds());
        checkLicenseDao.updateRoleModulePerMapper(null,false,LicenseModuleldNonControlEnum.NON_CONTROL.getModuleIds());
    }

    private void dispalyColumn(Boolean result, List<Integer> moduleIds){
        if(moduleIds != null && moduleIds.size() > 0){
            List<Integer> temp = new ArrayList<>(moduleIds);
            if(result){
                temp.addAll(checkLicenseDao.selectMwModuleByIds(moduleIds));
                temp.addAll(checkLicenseDao.selectMwModuleByPids(moduleIds));
                checkLicenseDao.updateMwModule(temp,false,null);
                checkLicenseDao.updateRoleModulePerMapper(temp,true,null);
            }else{
                for (Integer mo:moduleIds) {
                    if(checkLicenseDao.selectCountMwModule(mo) <=0){
                        temp.add(checkLicenseDao.selectMwModuleById(mo));
                    }
                }
                checkLicenseDao.updateMwModule(temp,true,null);
                checkLicenseDao.updateRoleModulePerMapper(temp,false,null);
            }
        }
    }

    @Override
    public void updateProp() throws Exception {
        //updateMwCustomcolTable();
        loadFilePrivateKey();
        loadFile();
        timeDown();
    }

    private void timeDown() throws ParseException {
        //HashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        Date now = new Date();
        long nowTime = now.getTime();
        if (redisTemplate.hasKey("moduleStart")) {
            long time = Long.parseLong(redisTemplate.opsForValue().get("moduleStart"));
            if(nowTime > time){
                redisTemplate.opsForValue().set("moduleStart", String.valueOf(nowTime));
            }else{
                nowTime = time;
            }
        }else{
            redisTemplate.opsForValue().set("moduleStart", String.valueOf(nowTime));
        }
        List<Integer> moduleIds = new ArrayList<>();
        for(String s : propMap.keySet()){
            LicenseXmlParam param = propMap.get(s);
            String expireDate = param.getExpireDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            long longDate = 0;
            if(expireDate != null && expireDate != ""){
                Date dt = sdf.parse(expireDate);
                longDate = dt.getTime()-nowTime;
                redisTemplate.opsForValue().set(s + "_date", String.valueOf(longDate));
            }else{
                redisTemplate.opsForValue().set(s + "_date", String.valueOf(0));
            }
            if(longDate > 0){
                if(LicenseModuleldEnum.getModuleIdsByName(s) != null){
                    moduleIds.addAll(LicenseModuleldEnum.getModuleIdsByName(s));
                }

            }
        }
        //dispalyColumn(true,moduleIds);
    }

    private void loadFilePrivateKey() throws Exception {
        log.info("开始初始化表");
        checkLicenseDao.initDeleteLicenseDatail();
        for (ModuleDesc val : ModuleDesc.values()) {
            LicenseXmlParam param = new LicenseXmlParam();
            param.setModuleId(val.getCode());
            param.setModuleName(val.getName());
            param.setModuleType("未激活");
            param.setImgUrl(val.getCode() + ".png");
            checkLicenseDao.initInsertLicenseDatail(param);
        }
        log.info("初始化成功 {}" ,filePath);
        File file = new File(filePath + "/mwProduceLicPrivateKey.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document parse = documentBuilder.parse(file);
        key = parse.getElementsByTagName("privateKey").item(0).getFirstChild().getNodeValue();
        NodeList companyNode = parse.getElementsByTagName("company");
        if(companyNode != null && companyNode.getLength() > 0){
            company = companyNode.item(0).getFirstChild().getNodeValue();
        }else{
            company = "安徽高颐科技有限公司";
        }
    }

    private void loadFile() throws Exception{
        log.info("开始加载许可信息");
        File file = new File(filePath + "/mwProduceLic.xml");
        log.info("文件路径" + filePath);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document parse = documentBuilder.parse(file);
        NodeList moduleKey = parse.getElementsByTagName("permit");
        String state = parse.getElementsByTagName("state").item(0).getFirstChild().getNodeValue();
        Integer stateDe = Integer.parseInt(Objects.requireNonNull(RSAUtils.decryptData(state, key)));
        for (int i = 0; i < moduleKey.getLength(); i++) {
            String hostMsg = parse.getElementsByTagName("hostMsg").item(i).getFirstChild().getNodeValue();
            String signature = parse.getElementsByTagName("signature").item(i).getFirstChild().getNodeValue();
            String moduleId = parse.getElementsByTagName("moduleKey").item(i).getFirstChild().getNodeValue();
            Node countNode = parse.getElementsByTagName("count").item(i).getFirstChild();
            String count = null;
            String describe = null;
            if(countNode != null){
                count = countNode.getNodeValue();
            }
            if(parse.getElementsByTagName("describe").item(i).getFirstChild() != null){
                describe = parse.getElementsByTagName("describe").item(i).getFirstChild().getNodeValue();
            }
            String signDe = RSAUtils.decryptData(signature, key);
            String moduleDe = RSAUtils.decryptData(moduleId, key);
            String hostMsgDe = RSAUtils.decryptData(hostMsg, key);
            String countDe = RSAUtils.decryptData(count, key);
            String describeDe = RSAUtils.decryptData(describe, key);
            String[] split = signDe.split("=");
            if (split.length == 2) {
                LicenseXmlParam param = new LicenseXmlParam();
                param.setHostMsg(hostMsgDe);
                param.setExpireDate(split[0]);
                if(countDe != null){
                    param.setCount(Integer.parseInt(countDe));
                }
                param.setDescribe(describeDe);
                param.setState(stateDe);
                propMap.put(split[1], param);
                log.info("module:{}, expire:{}",moduleDe,param.getExpireDate());
            } else {
                LicenseXmlParam param = new LicenseXmlParam();
                param.setCode(2);
                param.setMsg("许可证书信息有误");
                propMap.put(moduleDe, param);
            }
        }
    }
}
