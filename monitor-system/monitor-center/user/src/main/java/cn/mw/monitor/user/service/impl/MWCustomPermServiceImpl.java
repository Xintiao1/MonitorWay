package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.user.service.MWCustomPermService;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Created by zy.quaee on 2021/6/21 9:49.
 **/
@Service
@Slf4j
public class MWCustomPermServiceImpl implements MWCustomPermService {
    @Override
    public Reply customModuleToRedis() {
        MwModuleServiceImpl service = (MwModuleServiceImpl)SpringUtils.getBean("mwModuleServiceImpl");
        try {
            service.moduleToRedis();
        }catch (Exception e) {
            log.error(" failed to customModuleToRedis :",e);
            return Reply.fail("刷新失败!","");
        }
        return Reply.ok();
    }

    @Override
    public Reply customNotCheckToRedis() {
        MWNotCheckUrlServiceImpl service = (MWNotCheckUrlServiceImpl)SpringUtils.getBean("mWNotCheckUrlServiceImpl");
        try {
            service.notCheckUrlToRedis();
        }catch (Exception e) {
            log.error(" failed to notCheckUrlToRedis :",e);
            return Reply.fail("刷新失败!","");
        }
        return Reply.ok();
    }
}
