package cn.mw.monitor.alert.defaultPlugin;

import cn.mw.monitor.plugin.alert.AlertPlugin;
import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.register.PluginRegistryWrapperContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class AlertPluginDefault implements AlertPlugin {

    private final String PLUGIN_ID = "huaxing-plugin";

    @Override
    public List<String> getUserNameByQyWechatId(String[] wechatIdLits) {
        try {
            log.info("getUserNameByQyWechatId wechatIdLits:" + wechatIdLits.length);
            NtsPlugin plugin = PluginRegistryWrapperContextHolder.getPluginRegistryWrapper(PLUGIN_ID);
            AlertPlugin alertPlugin = plugin.getPluginApplicationContext().getBean(AlertPlugin.class);
            log.info("getUserNameByQyWechatId alertPlugin:" + alertPlugin);
            if (alertPlugin != null) {
                log.info("getUserNameByQyWechatId result:" + alertPlugin.getUserNameByQyWechatId(wechatIdLits));
                return alertPlugin.getUserNameByQyWechatId(wechatIdLits);
            }
        } catch (Exception e) {
            log.error("saveLdapUser error ", e);
        }
        return null;
    }
}
