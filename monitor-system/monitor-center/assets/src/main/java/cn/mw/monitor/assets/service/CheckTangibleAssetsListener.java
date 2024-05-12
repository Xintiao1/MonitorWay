package cn.mw.monitor.assets.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.event.EventListner;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/26
 */
public interface CheckTangibleAssetsListener extends EventListner {
    public List<Reply> processCheckTangibleAssets(AddUpdateTangAssetsParam aParam,boolean isAdd);
}
