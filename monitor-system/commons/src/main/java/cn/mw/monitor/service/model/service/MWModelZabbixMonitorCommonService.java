package cn.mw.monitor.service.model.service;

import cn.mw.monitor.service.model.param.MwModelAssetsInterfaceParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2023/5/31
 */
public interface MWModelZabbixMonitorCommonService {
   Reply updateInterfaceDescById(MwModelAssetsInterfaceParam param);

   Reply batchUpdateInterfaceShow(MwModelAssetsInterfaceParam param);

   Reply updateAlertTag(MwModelAssetsInterfaceParam param);
}
