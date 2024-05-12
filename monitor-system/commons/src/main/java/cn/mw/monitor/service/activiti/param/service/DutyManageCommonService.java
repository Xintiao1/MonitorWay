package cn.mw.monitor.service.activiti.param.service;


import cn.mw.monitor.service.activiti.param.DutyCommonsParam;

import java.util.HashSet;

/**
 * @author
 * @Date
 * @Version
 */
public interface DutyManageCommonService {

    HashSet<Integer> getDutyUserIds(DutyCommonsParam param);

}
