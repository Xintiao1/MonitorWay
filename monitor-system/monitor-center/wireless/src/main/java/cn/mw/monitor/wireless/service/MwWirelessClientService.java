package cn.mw.monitor.wireless.service;

import cn.mw.monitor.wireless.dto.QueryWirelessClientParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2021/6/16
 */
public interface MwWirelessClientService {
    Reply getClientInfo(QueryWirelessClientParam param);
}
