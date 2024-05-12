package cn.mw.monitor.assets.service;

import cn.mw.monitor.assets.dto.AssetsSyncDelayParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2021/7/2
 */
public interface MwAssetsSyncDelayService {
    Reply getDelayTable(AssetsSyncDelayParam param);
}
