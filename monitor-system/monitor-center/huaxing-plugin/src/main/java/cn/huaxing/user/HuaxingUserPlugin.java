package cn.huaxing.user;

import cn.huaxing.user.service.UserService;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.plugin.user.UserPlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @author guiquanwnag
 * @datetime 2023/8/23
 * @Description 华星的用户插件
 */
@Slf4j
@Service
public class HuaxingUserPlugin implements UserPlugin {

    @Autowired
    private UserService userService;

    /**
     * 存储用户信息
     */
    @Override
    public TimeTaskRresult saveLdapUser() {
        log.info("HuaxingUserPlugin start saveAndUpdateUser");
        TimeTaskRresult result = new TimeTaskRresult();
        userService.saveAndUpdateUser();
        log.info("HuaxingUserPlugin end saveAndUpdateUser");
        result.setSuccess(true);
        return result;
    }
}
