package cn.mw.monitor.util.service;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.util.entity.EmailFrom;

public interface MwEmailManageService {

    public EmailFrom selectEmailFrom(String ruleId);

    public EmailFrom selectEmailFromByName(String ruleName);

}
