package cn.mw.monitor.server.service;

import cn.mw.monitor.server.param.UpdateItemNameParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author syt
 * @Date 2021/8/24 15:19
 * @Version 1.0
 */
public interface MwItemNameService {

    Reply updateItemChName(UpdateItemNameParam param);
}
