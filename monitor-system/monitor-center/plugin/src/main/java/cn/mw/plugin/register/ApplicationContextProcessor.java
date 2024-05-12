package cn.mw.plugin.register;

import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.PluginPipeProcessor;
import cn.mw.plugin.PluginRegistContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;



/**
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public class ApplicationContextProcessor implements PluginPipeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextProcessor.class);
    @Override
    public void initialize() {

    }

    @Override
    public void registry(NtsPlugin ntsPlugin ,PluginRegistContext pluginRegistContext) throws Exception {
        logger.debug("ApplicationContextProcessor plugin-" + ntsPlugin.getPluginWrapper().getPluginId() + " context contain:\n"
            + Arrays.toString(ntsPlugin.getPluginApplicationContext().getBeanDefinitionNames()));
        ntsPlugin.getPluginApplicationContext().setClassLoader(ntsPlugin.getPluginWrapper().getPluginClassLoader());
        ntsPlugin.getPluginApplicationContext().getDefaultListableBeanFactory()
            .registerSingleton(ntsPlugin.getPluginWrapper().getPluginId(),
                ntsPlugin.getPluginWrapper().getPlugin());
        ntsPlugin.getPluginApplicationContext().refresh();
        logger.info("ApplicationContextProcessor plugin-[{}] context refresh end", ntsPlugin.getPluginWrapper().getPluginId());
    }

    @Override
    public void unRegistry(NtsPlugin ntsPlugin) throws Exception {
        ntsPlugin.getPluginApplicationContext().getDefaultListableBeanFactory().destroySingletons();
        // ntsPlugin.getPluginApplicationContext().close();
    }

}
