package cn.mw.monitor.wireless.service;

import cn.mw.monitor.wireless.dto.QueryWirelessDataShowParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2021/6/23
 */
public interface MwWirelessDataShowService {
    Reply getUserNumByTime(QueryWirelessDataShowParam param);

    Reply getFlowByTime(QueryWirelessDataShowParam param);

    Reply getDataByTXBytes(QueryWirelessDataShowParam param);

    Reply getDataByRXBytes(QueryWirelessDataShowParam param);

    Reply getDataByRSSI(QueryWirelessDataShowParam param);

    Reply getWirelessDeviceInfo(QueryWirelessDataShowParam param);

    Reply getRSSIDeviceInfo(QueryWirelessDataShowParam param);
}
