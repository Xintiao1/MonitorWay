package cn.mw.monitor.service.model.service;

import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.scan.param.QueryScanResultParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author qzg
 * @date 2022/7/21
 */
public interface MwModelAssetsByESService {
    Reply doInsertAssetsByES(AddUpdateTangAssetsParam aParam, boolean isbatch);

    Reply scanResultSearch(QueryScanResultParam queryScanResultParam) throws Exception;

    Reply doInsertAssetsToESByView(AddUpdateTangAssetsParam aParam, boolean isbatch) throws Throwable;


    Reply batchAddModelAssetsByModelView(List<AddUpdateTangAssetsParam> addParams) throws Exception;
}
