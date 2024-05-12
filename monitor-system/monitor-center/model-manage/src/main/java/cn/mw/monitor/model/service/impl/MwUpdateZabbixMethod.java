package cn.mw.monitor.model.service.impl;

import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author qzg
 * @date 2023/3/23
 */
@Service
public class MwUpdateZabbixMethod {
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwModelAssetsDiscoveryServiceImpl mwModelAssetsDiscoveryServiceImpl;

    public Boolean updateHostState(Integer serverId, List<String> hostIds, Integer status){
        MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.hostUpdate(serverId, hostIds, status);
        Boolean isFlag = false;
        if (mwZabbixAPIResult==null || mwZabbixAPIResult.isFail()) {
            isFlag = true;
        }
        return isFlag;
    }

    public Boolean batchOpenNowItems(Integer serverId, List<String> hostIds, Integer status){
        Boolean aBoolean = mwModelAssetsDiscoveryServiceImpl.batchOpenNowItems(serverId, hostIds);
        return aBoolean;
    }


}
