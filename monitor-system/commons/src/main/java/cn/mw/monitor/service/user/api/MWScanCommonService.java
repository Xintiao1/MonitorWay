package cn.mw.monitor.service.user.api;

import cn.mw.monitor.service.user.model.ScanIpAddressManageQueueVO;

import java.util.List;

/**
 * @author lumingming
 * @createTime 202211-1616 10:39
 * @description 扫描全局参数
 */
public interface MWScanCommonService {

    List<ScanIpAddressManageQueueVO> selectqueueList(Integer integer);
}
