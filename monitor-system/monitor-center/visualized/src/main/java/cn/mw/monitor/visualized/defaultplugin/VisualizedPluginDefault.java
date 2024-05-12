package cn.mw.monitor.visualized.defaultplugin;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.plugin.user.UserPlugin;
import cn.mw.monitor.plugin.visualized.VisualizedPlugin;
import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.register.PluginRegistryWrapperContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author gengjb
 * @description TODO
 * @date 2023/9/13 16:48
 */
@Slf4j
@Service
public class VisualizedPluginDefault implements VisualizedPlugin {

    private final String PLUGIN_ID = "huaxing-plugin";

    @Override
    public TimeTaskRresult saveCaheData() {
        try {
            NtsPlugin plugin = PluginRegistryWrapperContextHolder.getPluginRegistryWrapper(PLUGIN_ID);
            VisualizedPlugin visualizedPlugin = plugin.getPluginApplicationContext().getBean(VisualizedPlugin.class);
            if (visualizedPlugin != null) {
                return visualizedPlugin.saveCaheData();
            }
        } catch (Exception e) {
            log.error("saveCaheData error ",e);
        }
        log.error("VisualizedPluginDefault saveCaheData");
        return new TimeTaskRresult().setSuccess(true);
    }
}
