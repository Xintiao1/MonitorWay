package cn.mw.monitor.license.service.impl;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.license.config.MWLicenseConfigLoad;
import cn.mw.monitor.license.util.ScheduleGetPermit;
import cn.mw.monitor.service.license.param.LicenseAssetsModuleStatusParam;
import cn.mw.monitor.service.license.param.LicenseFieldEnum;
import cn.mw.monitor.service.license.param.LicenseXmlParam;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//许可管理
@Service
public class LicenseManagementServiceImpl implements LicenseManagementService {

    @Autowired
    ScheduleGetPermit scheduleGetPermit;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Logger log = LoggerFactory.getLogger("MWLicenseController");


    @Override
    public ResponseBase getLicenseManagemengt(String moduleName, Integer count, Integer addNum){
        ConcurrentHashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        LicenseXmlParam param = propMap.get(moduleName);
        log.info("LicenseManagementServiceImpl{} getLicenseManagemengt()"+param);
        if(count + addNum <= param.getCount() || param.getCount() >= 999999){
            RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
            redisUtils.set(moduleName, count + addNum);
            return new ResponseBase(Constants.HTTP_RES_CODE_200,null, null);
        }
        return new ResponseBase(Constants.HTTP_RES_CODE_500,"该模块新增数量已达许可数量上限！", null);
    }

    @Override
    public ResponseBase getLicenseManagemengtAssets(Integer assetsTypeId, Integer count, Integer addNum){
        log.info("普通添加资产：" + assetsTypeId);
        String moduleName = LicenseManagementEnum.getModuleName(assetsTypeId);
        log.info("moduleName：" + moduleName);
        if(moduleName == null){
            return new ResponseBase(Constants.HTTP_RES_CODE_200,null, null);
        }
        ConcurrentHashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        int permit = scheduleGetPermit.getPermit(moduleName);
        LicenseXmlParam param = propMap.get(moduleName);
        if(permit == 0 || permit == 7 || permit == 8){
            if(count + addNum <= param.getCount() || param.getCount() >= 999999){
                RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
                redisUtils.set(moduleName, count + addNum);
                return new ResponseBase(Constants.HTTP_RES_CODE_200,null, null);
            }
            return new ResponseBase(Constants.HTTP_RES_CODE_500,ModuleNameDesc.getModuleDescEnum(assetsTypeId) + "新增数量已达许可数量上限！", null);
        }
        String msg=null;
        switch (permit){
            case 1:
                msg="许可文件加载失败！";
                break;
            case 2:
                msg= ModuleNameDesc.getModuleDescEnum(assetsTypeId)+"module not licensed,please reapply";
                break;
            case 3:
                msg="The SN is incorrect,please upload the correct license file";
                break;
            case 4:
                msg= ModuleNameDesc.getModuleDescEnum(assetsTypeId)+ "使用已过期,请重新激活！";
                break;
            case 6:
                msg= ModuleNameDesc.getModuleDescEnum(assetsTypeId)+ "is stop,please go to configuration";
                break;
            default:
                msg="unkonw erro";
        }
        return new ResponseBase(Constants.HTTP_RES_CODE_500,msg, null);
    }

    @Override
    public ResponseBase getLicenseManagemengtAssetsByMonitorMode(Integer assetsTypeId, Integer monitorMode, Integer count, Integer addNum){
        return getLicenseManagemengtAssets(assetsTypeId, count, addNum);
    }

    @Override
    public LicenseAssetsModuleStatusParam getModuleStatus(LicenseAssetsModuleStatusParam param) {
        LicenseAssetsModuleStatusParam licenseAssetsParam = new LicenseAssetsModuleStatusParam();
        licenseAssetsParam.setOperationState(getModuleStatus(ModuleNameEnum.MW_MONITOR.getModuleName() ,param.getOperationCount()));
        licenseAssetsParam.setAutoState(getModuleStatus(ModuleNameEnum.AUTOMANAGE.getModuleName() ,param.getAutoCount()));
        licenseAssetsParam.setPropState(getModuleStatus(ModuleNameEnum.PROP_MANAGE.getModuleName() ,param.getPropCount()));
        licenseAssetsParam.setLogState(getModuleStatus(ModuleNameEnum.LOG_SECURITY.getModuleName() ,param.getLogCount()));
        return licenseAssetsParam;
    }

    public Boolean getModuleStatus(String moduleName,Integer count) {
        ConcurrentHashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        LicenseXmlParam param = propMap.get(moduleName);
        if(param == null ||param.getCount() == null || (param.getCount() <= count && param.getCount() <= 999999)){
            return false;
        }
        if(param.getState().equals(LicenseEnum.subscribed.getChNum())){
            return true;
        }else {
            //数量匹配
            return Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(moduleName + AlertAssetsEnum.UNDERLINE.toString() + LicenseFieldEnum.DATE.toString()))) > 0;
        }

    }

}
