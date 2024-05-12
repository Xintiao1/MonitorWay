package cn.mw.monitor.wireless.service;

import cn.mw.monitor.wireless.api.param.QueryWirelessAPParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author syt
 * @Date 2021/6/21 10:20
 * @Version 1.0
 */
public interface MwWirelessAPService {

    Reply getAPTableInfo(QueryWirelessAPParam param);
}
