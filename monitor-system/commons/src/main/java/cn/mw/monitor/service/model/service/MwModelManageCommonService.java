package cn.mw.monitor.service.model.service;

import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2021/05/15 9:03
 */
public interface MwModelManageCommonService {
    Reply getAllModelGroupInfo();

    Reply getAssetsSubTypeByMode();

    Reply getMonitorModeInfo();


}
