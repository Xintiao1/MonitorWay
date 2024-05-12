package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.superfusion.MwQuerySuperFusionParam;
import cn.mw.monitor.model.param.superfusion.QuerySuperFusionHistoryParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2023/7/24
 */
public interface MwModelSuperFusionService {
    String getPublicKey();

    Reply getSuperFusionStorageInfo(QuerySuperFusionHistoryParam param);

    Reply getSuperFusionVmInfo(QuerySuperFusionHistoryParam param);

    Reply getSuperFusionHostInfo(QuerySuperFusionHistoryParam param);

    Reply getSuperFusionBaseInfo();

    Reply getAllStorageList(MwQuerySuperFusionParam mParam);

    Reply getAllVmList(MwQuerySuperFusionParam mParam);

    Reply getAllHostList(MwQuerySuperFusionParam mParam);

    Reply getSuperFusionTree(MwQuerySuperFusionParam mParam);

    Reply getSuperFusionHostHistory(QuerySuperFusionHistoryParam param);

    Reply saveSuperFusionDeviceData(MwQuerySuperFusionParam mParam);

}
