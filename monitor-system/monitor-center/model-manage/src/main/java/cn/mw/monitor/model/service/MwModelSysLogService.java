package cn.mw.monitor.model.service;

import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.model.param.SystemLogParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author qzg
 * @date 2021/11/11
 */
public interface MwModelSysLogService {
    Reply getInstaceChangeHistory(SystemLogParam qParam);

    Reply saveInstaceChangeHistory(SystemLogDTO qParam);

    Reply batchSaveInstaceChangeHistory(List<SystemLogDTO> qParams);

    Reply getChangeHistoryVersion(String type);

    Reply updateInstaceChangeHistory(String targetType,String ownType);
}
