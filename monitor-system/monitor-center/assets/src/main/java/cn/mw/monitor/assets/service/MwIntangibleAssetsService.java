package cn.mw.monitor.assets.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.assets.api.param.assets.*;
import cn.mw.monitor.service.assets.param.UpdateAssetsStateParam;
import cn.mw.monitor.assets.api.param.assets.AddUpdateIntangAssetsParam;

import java.util.List;


/**
 * Created by baochengbin on 2020/3/12.
 */
public interface MwIntangibleAssetsService {

    Reply selectById(String id);

    Reply selectList(QueryIntangAssetsParam mtaDTO);

    Reply update(AddUpdateIntangAssetsParam mtaDTO);

    Reply delete(List<String> ids);

    Reply insert(AddUpdateIntangAssetsParam mtaDTO);

    Reply updateState(UpdateAssetsStateParam updateAssetsStateParam);

//    Reply getDropdown();
}
