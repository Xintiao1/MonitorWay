package cn.mw.monitor.service.assets.service;

import cn.mw.monitor.service.assets.model.AssetsInterfaceDTO;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.assets.param.RefreshInterfaceParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/5/11
 */
public interface MwAssetsInterfaceService {
    Reply getAllInterface(QueryAssetsInterfaceParam param);

    Reply setInterfaceStatus(QueryAssetsInterfaceParam param);

    void refreshInterfaceInfo(RefreshInterfaceParam refreshInterfaceParam);

    /**
     * 获取资产的所有接口
     * @param param 参数
     * @return
     */
    Reply getAllInterfaces(QueryAssetsInterfaceParam param);

    List<AssetsInterfaceDTO> getAllAssetsInterfaceByCriteria(Map map);
}
