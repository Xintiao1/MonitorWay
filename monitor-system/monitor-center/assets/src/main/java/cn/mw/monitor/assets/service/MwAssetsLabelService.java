package cn.mw.monitor.assets.service;

import cn.mw.monitor.assets.param.QueryAssetsLabelParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author syt
 * @Date 2021/7/22 16:54
 * @Version 1.0
 */
public interface MwAssetsLabelService {
    Reply selectLabelList(QueryAssetsLabelParam qParam);
}
