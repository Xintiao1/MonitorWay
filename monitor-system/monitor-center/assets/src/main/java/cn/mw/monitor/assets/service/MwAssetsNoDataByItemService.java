package cn.mw.monitor.assets.service;

import cn.mw.monitor.assets.dto.AssetsSyncDelayParam;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * @date 2021/10/14
 */
public interface MwAssetsNoDataByItemService {
    Reply getNoDataAssets(HttpServletRequest request, HttpServletResponse response);
}
