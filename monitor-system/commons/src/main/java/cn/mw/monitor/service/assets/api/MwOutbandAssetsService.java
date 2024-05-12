package cn.mw.monitor.service.assets.api;

import cn.mw.monitor.service.assets.param.AddUpdateOutbandAssetsParam;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.assets.param.QueryOutbandAssetsParam;
import cn.mw.monitor.service.assets.param.UpdateAssetsStateParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author syt
 * @Date 2020/6/19 16:02
 * @Version 1.0
 */
public interface MwOutbandAssetsService {

    Reply selectById(String id);

    Reply selectList(QueryOutbandAssetsParam qParam);

    Reply updateAssets(AddUpdateOutbandAssetsParam uParam) throws Throwable;

    Reply deleteAssets(List<DeleteTangAssetsID> ids);

    Reply insertAssets(AddUpdateOutbandAssetsParam aParam) throws Throwable;

    Reply doInsertAssets(AddUpdateOutbandAssetsParam aParam) throws Throwable;

//    Reply selectAllLabel(QueryOutbandAssetsParam queryTangAssetsParam);

    Reply updateState(UpdateAssetsStateParam updateAssetsStateParam);

    String createAndGetZabbixHostId(AddUpdateOutbandAssetsParam aParam);

    Reply selectDropdownList();

    Reply updateAssetsTemplateIds();

    /**
     * 带外资产模糊查询
     *
     * @return
     */
    Reply outBandAssetsFuzzyQuery();
}
