package cn.mw.monitor.service.model.listener;

import cn.mw.monitor.event.EventListner;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/26
 */
public interface CheckMWModelAssetsListener extends EventListner {
    public List<Reply> processCheckModelAssets(AddUpdateTangAssetsParam aParam, boolean isAdd);
}
