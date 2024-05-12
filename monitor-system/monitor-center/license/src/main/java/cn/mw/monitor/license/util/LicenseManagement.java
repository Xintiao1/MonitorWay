/*
移到serviceImp层
package cn.mw.monitor.license.util;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.ModuleDesc;
import cn.mw.monitor.license.config.MWLicenseConfigLoad;
import cn.mw.monitor.service.license.param.LicenseXmlParam;
import cn.mw.monitor.util.LicenseManagementEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

//许可管理
@Component
public class LicenseManagement {

    @Autowired
    ScheduleGetPermit scheduleGetPermit;

    public ResponseBase getLicenseManagemengt(String moduleName, Integer count, Integer addNum){
        HashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        LicenseXmlParam param = propMap.get(moduleName);
        if(count + addNum <= param.getCount()){
            return new ResponseBase(Constants.HTTP_RES_CODE_200,null, null);
        }
        return new ResponseBase(Constants.HTTP_RES_CODE_500,"该模块新增数量已达许可数量上限！", null);
    }

    public ResponseBase getLicenseManagemengtAssets(Integer assetsTypeId, Integer count, Integer addNum){
        String moduleName = LicenseManagementEnum.getModuleName(assetsTypeId);
        if(moduleName == null){
            return new ResponseBase(Constants.HTTP_RES_CODE_200,null, null);
        }
        HashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        int permit = scheduleGetPermit.getPermit(moduleName);
        if(permit == 0){
            LicenseXmlParam param = propMap.get(moduleName);
            if(count + addNum <= param.getCount()){
                return new ResponseBase(Constants.HTTP_RES_CODE_200,null, null);
            }
            return new ResponseBase(Constants.HTTP_RES_CODE_500,"该模块新增数量已达许可数量上限！", null);
        }
        String msg=null;
        switch (permit){
            case 1:
                msg="Failed to load product license information";
                break;
            case 2:
                msg= ModuleDesc.getModuleDescEnum(moduleName)+"module not licensed,please reapply";
                break;
            case 3:
                msg="The SN is incorrect,please upload the correct license file";
                break;
            case 4:
                msg= ModuleDesc.getModuleDescEnum(moduleName)+ "usage time expires,please reapply";
                break;
            case 6:
                msg= ModuleDesc.getModuleDescEnum(moduleName)+ "is stop,please go to configuration";
                break;
            default:
                msg="unkonw erro";
        }
        return new ResponseBase(Constants.HTTP_RES_CODE_500,msg, null);
    }
    public ResponseBase getLicenseManagemengtAssetsByMonitorMode(Integer assetsTypeId, List<Integer> monitorMode, Integer count, Integer addNum){
        String moduleName = LicenseManagementEnum.getModuleName(assetsTypeId);
        if(moduleName == null){
            return new ResponseBase(Constants.HTTP_RES_CODE_200,null, null);
        }
        HashMap<String, LicenseXmlParam> propMap = MWLicenseConfigLoad.propMap;
        int permit = scheduleGetPermit.getPermit(moduleName);
        if(permit == 0){
            LicenseXmlParam param = propMap.get(moduleName);
            if(count + addNum <= param.getCount()){
                return new ResponseBase(Constants.HTTP_RES_CODE_200,null, null);
            }
            return new ResponseBase(Constants.HTTP_RES_CODE_500,"该模块新增数量已达许可数量上限！", null);
        }
        String msg=null;
        switch (permit){
            case 1:
                msg="Failed to load product license information";
                break;
            case 2:
                msg= ModuleDesc.getModuleDescEnum(moduleName)+"module not licensed,please reapply";
                break;
            case 3:
                msg="The SN is incorrect,please upload the correct license file";
                break;
            case 4:
                msg= ModuleDesc.getModuleDescEnum(moduleName)+ "usage time expires,please reapply";
                break;
            case 6:
                msg= ModuleDesc.getModuleDescEnum(moduleName)+ "is stop,please go to configuration";
                break;
            default:
                msg="unkonw erro";
        }
        return new ResponseBase(Constants.HTTP_RES_CODE_500,msg, null);
    }

}
*/
