package cn.mw.monitor.user.defaultplugin;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.plugin.user.UserPlugin;
import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.register.PluginRegistryWrapperContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guiquanwnag
 * @datetime 2023/8/23
 * @Description
 */
@Slf4j
@Service
public class UserPluginDefault implements UserPlugin {

    private final String PLUGIN_ID = "huaxing-plugin";

    /**
     * 存储用户信息
     */
    @Override
    public TimeTaskRresult saveLdapUser() {
        try {
            NtsPlugin plugin = PluginRegistryWrapperContextHolder.getPluginRegistryWrapper(PLUGIN_ID);
            UserPlugin userPlugin = plugin.getPluginApplicationContext().getBean(UserPlugin.class);
            if (userPlugin != null) {
                return userPlugin.saveLdapUser();
            }
        } catch (Exception e) {
            log.error("saveLdapUser error ",e);
        }
        log.error("UserPluginDefault saveLdapUser");
        //do nothing
        return new TimeTaskRresult().setSuccess(true);
    }
}
