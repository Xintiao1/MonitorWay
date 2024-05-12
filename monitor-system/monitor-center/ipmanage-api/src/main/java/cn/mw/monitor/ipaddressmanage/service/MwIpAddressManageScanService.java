package cn.mw.monitor.ipaddressmanage.service;

import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.service.user.model.ScanIpAddressManageQueueVO;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.ipaddressmanage.param.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface MwIpAddressManageScanService {

    public static final String PREFIX = "ip_manage";

    public static final String SCAN_SUCCESS = "扫描更新成功";

    public static final String SCAN_ERROR = "扫描更新错误";

    //批量扫描
    Reply batchScanIp(List<AddUpdateIpAddressManageListParam> uParam);

    Reply batchScanIp(List<AddUpdateIpAddressManageListParam> uParam, Integer linkId, MWUser userInfo);

    Reply getHisList(AddUpdateIpAddressManageListParam parm) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    void updateAssetsType(Integer linkid);

    Reply canleBatchScan(Integer linkId);

    void addScanQueue(String parm, Integer userId, Integer linkId);

    ScanIpAddressManageQueueVO selectqueue();

    List<ScanIpAddressManageQueueVO>  selectqueueList(Integer integer);

    void deleteQueue(Integer linkId, Integer id);

    //初始化页子节点数量，递归的开始,更新统计信息
    void recursiveIp(Set<Integer> ids);

    //获取ip地址状态
    int extractIPState(AddUpdateIpAddressManageListParam addIp);

    //更新ip状态及资产信息
    void updateIpStateAndAssetInfo(List<AddUpdateIpAddressManageListParam> uParam , Map<String, List<IPInfoDTO>> scanRes);
}
