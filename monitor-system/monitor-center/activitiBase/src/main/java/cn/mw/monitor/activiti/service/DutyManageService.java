package cn.mw.monitor.activiti.service;

import cn.mw.monitor.activiti.param.DutyManageParam;
import cn.mw.monitor.activiti.param.DutyShiftParam;
import cn.mwpaas.common.model.Reply;
import java.util.List;

/**
 * @author
 * @Date
 * @Version
 */
public interface DutyManageService {

    Reply createDuty(List<DutyManageParam> params);

    Reply queryDuty(DutyManageParam param);

    Reply showDuty(DutyManageParam param);

    Reply deleteDuty(String id);

    Reply shiftCreate(DutyShiftParam param);

    Reply shiftBrowse(DutyShiftParam param);

    Reply shiftDelete(List<String> ids);

    Reply shiftEditorBefore(String id);

    Reply shiftEditor(DutyShiftParam param);

    Reply dropBrowse(DutyShiftParam param);

}
