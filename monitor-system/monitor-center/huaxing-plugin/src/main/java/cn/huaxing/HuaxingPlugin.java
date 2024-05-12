package cn.huaxing;

import cn.mw.plugin.NtsBasePlugin;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;

@Slf4j
public class HuaxingPlugin extends NtsBasePlugin {
    public HuaxingPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    public void start() {
        log.info("HuaxingPlugin start()");
        super.start();
    }
}
