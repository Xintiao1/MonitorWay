package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.monitor.util.service.MwEmailManageService;
import cn.mw.monitor.weixin.dao.MwWeixinTemplateDao;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;


/**
 *
 */
@Service
public class MwEmaiManageImpl implements MwEmailManageService {

    @Resource
    private MwWeixinTemplateDao mwWeixinTemplateDao;


    @Override
    public EmailFrom selectEmailFrom(String ruleId) {
        EmailFrom emailFrom = mwWeixinTemplateDao.selectEmailFromCommon(ruleId);
        return emailFrom;
    }

    @Override
    public EmailFrom selectEmailFromByName(String ruleName) {
        EmailFrom emailFrom = mwWeixinTemplateDao.selectEmailByNameCommon(ruleName);
        return emailFrom;
    }

}
