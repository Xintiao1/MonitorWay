package cn.mw.module.security.service;

import cn.mw.module.security.dto.EsSysLogRuleDTO;
import cn.mw.module.security.dto.EsSysLogTagDTO;
import cn.mwpaas.common.model.Reply;

/**
 * @author qzg
 * @date 2022/1/6
 */
public interface EsSysLogRuleService {
    Reply getSystemLogRulesInfos(EsSysLogRuleDTO param);

    Reply getRulesInfoById(EsSysLogRuleDTO param);

    Reply createSysLogRulesInfo(EsSysLogRuleDTO param);

    Reply editoSysLogRulesInfo(EsSysLogRuleDTO param);

    Reply updateSysLogRulesState(EsSysLogRuleDTO param);

    Reply deleteSysLogRulesInfo(EsSysLogRuleDTO param);

    Reply createTagInfo(EsSysLogTagDTO param);

    Reply deleteSysLogTagInfo(EsSysLogTagDTO param);

    Reply getSysLogTagInfo();

    Reply fuzzSearchAllFiledData();

}
