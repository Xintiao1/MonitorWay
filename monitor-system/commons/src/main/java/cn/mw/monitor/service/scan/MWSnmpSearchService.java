package cn.mw.monitor.service.scan;

import cn.mw.monitor.service.assets.param.DeviceScanContext;
import cn.mw.monitor.service.scan.model.Device;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mw.monitor.service.scan.param.SearchParam;
import cn.mw.monitor.service.scan.param.SnmpSearchAction;

import java.util.List;

public interface MWSnmpSearchService {
    public static final String SCAN_SERVICE = "scan";

    List<ScanResultSuccess> searchRedundantResult(List<ScanResultSuccess> list);
    String searchDeviceCode(DeviceScanContext deviceScanContext);
    void shutdownScan() throws Exception;

    //获取设备接口信息
    List<Device> findDeviceInterfaceInfo(List<ScanResultSuccess> list ,SnmpSearchAction snmpSearchAction);
    List<Device> findDeviceInterfaceInfoBySearchParam(List<SearchParam> searchParams ,SnmpSearchAction snmpSearchAction);

    void setSnmpSearchThreadLocal(String key);
    void cleanThreadLocal();
    void registerThreadLocalAction(String key , SnmpSearchAction snmpSearchAction);
}
