package cn.mw.plugin;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * .
 * @author qiyao(1210)
 * @date 2022-03-04
 */
public abstract class NtsBasePlugin extends Plugin {
    protected final String basePackage;

    public NtsBasePlugin(PluginWrapper wrapper) {
        super(wrapper);
        this.basePackage = this.getClass().getPackage().getName();
    }

    @Override
    public void start() {
        log.info("start plugin: {}",this.wrapper.getPluginId());
    }

    @Override
    public void stop() {
        log.info("stop plugin: {}",this.wrapper.getPluginId());
    }
}
