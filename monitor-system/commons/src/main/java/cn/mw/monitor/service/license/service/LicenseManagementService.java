package cn.mw.monitor.service.license.service;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.license.param.LicenseAssetsModuleStatusParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;

public interface LicenseManagementService {
    //出资产模块调用方法
    public ResponseBase getLicenseManagemengt(String moduleName, Integer count, Integer addNum);
    //资产模块根据资产类型调用方法
    public ResponseBase getLicenseManagemengtAssets(Integer assetsTypeId, Integer count, Integer addNum);
    //资产模块根据资产类型与监控方式调用方法
    public ResponseBase getLicenseManagemengtAssetsByMonitorMode(Integer assetsTypeId, Integer monitorMode, Integer count, Integer addNum);

    public LicenseAssetsModuleStatusParam getModuleStatus(LicenseAssetsModuleStatusParam param);
}
