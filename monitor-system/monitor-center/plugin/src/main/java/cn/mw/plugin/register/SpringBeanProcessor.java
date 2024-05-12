package cn.mw.plugin.register;

import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.PluginPipeProcessor;
import cn.mw.plugin.PluginRegistContext;
import cn.mw.plugin.register.classgroup.SchedulingGroup;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import java.util.List;


/**
 * @author qiyao(1210)
 * @date
 */
public class SpringBeanProcessor implements PluginPipeProcessor {
    @Override
    public void initialize() {

    }

    @Override
    public void registry(NtsPlugin ntsPlugin, PluginRegistContext pluginRegistContext) throws Exception {
        List<Class<?>> classes = ntsPlugin.getClassList("plugin_component");
        if (classes.isEmpty()) {
            return;
        }
        SchedulingGroup schedulingGroup = new SchedulingGroup();
        // @EnableScheduling
        if (classes.stream().anyMatch(schedulingGroup::filter)
        || schedulingGroup.filter(ntsPlugin.getPluginWrapper().getPlugin().getClass())) {
            ntsPlugin.getPluginApplicationContext().register(ScheduledAnnotationBeanPostProcessor.class);
        }
        ntsPlugin.getPluginApplicationContext().register(classes.toArray(new Class[0]));
    }

    @Override
    public void unRegistry(NtsPlugin ntsPlugin) throws Exception {
        ntsPlugin.getPluginApplicationContext().close();
    }
}
