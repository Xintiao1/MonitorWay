package cn.mw.xiangtai.plugin;

import cn.mw.plugin.NtsBasePlugin;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;

@Slf4j
public class XiangtaiPlugin extends NtsBasePlugin {
    public XiangtaiPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("XiangtaiPlugin start()");
        super.start();
    }
}
