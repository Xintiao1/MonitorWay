package cn.mw.monitor.license.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.common.constant.ModuleDesc;
import cn.mw.monitor.license.config.MWLicenseConfigLoad;
import cn.mw.monitor.license.dao.MwCheckLicenseDao;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.license.param.LicenseXmlParam;
import cn.mw.monitor.service.license.param.QueryLicenseParam;
import cn.mw.monitor.service.license.service.CheckLicenseService;
import cn.mw.monitor.util.RedisUtils;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CheckLicenseServiceImpl implements CheckLicenseService {
    private static final Logger log = LoggerFactory.getLogger("MWLicenseController");
    @Resource
    MwCheckLicenseDao mwCheckLicenseDao;

    public Set<QueryLicenseParam> paramList=new HashSet<>();

    public Set<String> moduleList=new HashSet<>();

    @Value("${alert.level}")
    private String alertLevel;

    @Override
    public void insertLicenseInfo(String moduleId, int day){
        //信息持久化 先过滤 防止频繁和数据库io交互
        QueryLicenseParam queryLicenseParam = new QueryLicenseParam();
        queryLicenseParam.setModuleId(moduleId);
        queryLicenseParam.setModuleName(ModuleDesc.getModuleDescEnum(moduleId));
        queryLicenseParam.setRemainDate(day);
        paramList.add(queryLicenseParam);
 }

    @Override
    public void deleteLicenseInfo(String moduleId){
        moduleList.add(moduleId);
    }

    @Override
    public Reply queryLicenseInfo() throws Exception {
        Reply reply=new Reply();
        List<QueryLicenseParam> queryLicenseParam = mwCheckLicenseDao.queryAllExpireLicense();
        reply.setData(queryLicenseParam);
        return reply;
    }

    @Override
    public Reply queryLicenseList() throws ParseException {
        ConcurrentHashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        //获取所有的key
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date conditionDate = sdf.parse("2099-01-01");
        for (HashMap.Entry<String, LicenseXmlParam> val:propMap.entrySet()) {
            LicenseXmlParam param = propMap.get(val.getKey());
            LicenseXmlParam licenseXml=new LicenseXmlParam();
            licenseXml.setModuleId(val.getKey());
            Date expireDate = sdf.parse(param.getExpireDate());
            int comparison = expireDate.compareTo(conditionDate);
            licenseXml.setExpireDate(param.getExpireDate());
            log.info("许可comparison:" + comparison);
            log.info("许可alertLevel:" + alertLevel);
            if(comparison >= 0 && !alertLevel.equals(AlertEnum.HUAXING.toString())){
                licenseXml.setExpireDate("永久");
            }
            licenseXml.setCount(param.getCount());
            if(ModuleDesc.getModuleDesc(val.getKey()) != null){
                String replace = "N/A";
                if(param.getCount() != null){
                    replace = param.getCount().toString();
                }
                licenseXml.setDescribe(ModuleDesc.getModuleDesc(val.getKey()).replaceAll("COUNT",replace));
            }
            boolean isHas = redisUtils.hasKey(val.getKey());
            log.info("ssss:" + val.getKey());
            Integer usedCount = 0;
            if (isHas) {
                usedCount = (Integer) redisUtils.get(val.getKey());
            }
            licenseXml.setUsedCount(usedCount);
            mwCheckLicenseDao.updateLicenseDatail(licenseXml);
        }
        List<LicenseXmlParam> licenseXmlParams = mwCheckLicenseDao.queryLicenseDatail();
        return Reply.ok(licenseXmlParams);
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void insertLicense() throws Exception{
        Iterator<QueryLicenseParam> iterator = paramList.iterator();
        while (iterator.hasNext()){
            QueryLicenseParam next = iterator.next();
            next.setCreateDate(new Date());
            try {
                mwCheckLicenseDao.insertLicense(next);
            }catch (Exception e){
                log.info("插入失败");
            }
            iterator.remove();
        }
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void deleteLicense() throws Exception{
        Iterator<String> iterator = moduleList.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            try {
                mwCheckLicenseDao.deleteLicenseByModuleId(next);
            }catch (Exception e){
                log.info("删除失败");
            }
            iterator.remove();
        }
    }
}
