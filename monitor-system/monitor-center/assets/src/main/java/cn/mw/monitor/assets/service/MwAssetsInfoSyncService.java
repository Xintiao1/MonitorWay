package cn.mw.monitor.assets.service;

import cn.mw.monitor.assets.api.param.assets.AddUpdateAssetsIotParam;
import cn.mw.monitor.assets.param.AssetsInfoSyncByNameParam;
import cn.mwpaas.common.model.Reply;

public interface MwAssetsInfoSyncService {
    Reply assetsInfoSync(String groupName);

    Reply syncAssetsNameReachZabbix(Integer type);
}
