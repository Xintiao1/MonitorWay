package cn.mw.monitor.service.systemLog.api;

import cn.mw.monitor.service.systemLog.dto.LoginLogDTO;
import cn.mw.monitor.service.systemLog.param.EditLogParam;
import cn.mw.monitor.service.systemLog.param.SystemLogParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2021/11/10
 */
public interface  MwSysLogService {
//    void save(MwSysLogEntity mwSysLogEntity);

    void saveLoginLog(LoginLogDTO userLogDTO);

//    Reply selectList(QueryLogParam qParam);

    Reply selectTableName(Integer tableNameKey);

    Reply selectSysLog(SystemLogParam qParam);

    Reply selectSysLogByModel(SystemLogParam qParam);

    Reply saveEditLog(EditLogParam param);

}
