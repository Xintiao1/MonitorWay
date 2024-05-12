package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.BatchUpdatePowerParam;
import cn.mw.monitor.model.param.CancelZabbixAssetsParam;
import cn.mw.monitor.model.param.ModelAddTangAssetsParam;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author qzg
 * @date 2022/7/12
 */
public interface MwModelAssestDiscoveryService {
    Reply addModelAssetsByScanSuccess(ModelAddTangAssetsParam param) throws Throwable;

    Reply addModelAssetsByInsert(ModelAddTangAssetsParam param) throws Throwable;

    Reply getTemplateListByMode(AddUpdateTangAssetsParam aParam);

    Reply modelAssetsToManage(QueryInstanceModelParam params) throws Exception;

    Reply updateListStatus(BatchUpdatePowerParam param);

    Reply deleteAssetsToZabbix(List<CancelZabbixAssetsParam> ids);

    Reply insertAssetsByCommon(AddUpdateTangAssetsParam param) throws Throwable;

}
