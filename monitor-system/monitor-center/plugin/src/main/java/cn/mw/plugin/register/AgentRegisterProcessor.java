package cn.mw.plugin.register;

import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.PluginPipeProcessor;
import cn.mw.plugin.PluginRegistContext;
import cn.mw.plugin.annotation.AgentMethod;
import cn.mw.plugin.extension.inner.ProxyMethodInteceptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @author qiyao(1210)
 * @date 2022-04-07
 */
public class AgentRegisterProcessor implements PluginPipeProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AgentRegisterProcessor.class);

    @Override
    public void initialize() {

    }

    @Override
    public void registry(NtsPlugin ntsPlugin ,PluginRegistContext pluginRegistContext) throws Exception {
        List<Class<?>> agentClasses = ntsPlugin.getClassList("agents");
        agentClasses.forEach(clazz -> {
            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                AgentMethod annotation = declaredMethod.getAnnotation(AgentMethod.class);
                if (annotation != null) {
                    Object o = ntsPlugin.getPluginApplicationContext().getDefaultListableBeanFactory().getBean(clazz);
                    if (!ProxyMethodInteceptorFactory.addProxyMethod(annotation.id(), o, declaredMethod)) {
                        logger.info("[{}]-[{}]-[{}] register agent method failed.", ntsPlugin.getPluginWrapper().getPluginId(),
                            clazz.getName(),declaredMethod.getName());
                    } else {
                        logger.info("[{}]-[{}]-[{}] register agent method success.", ntsPlugin.getPluginWrapper().getPluginId(),
                            clazz.getName(),declaredMethod.getName());
                    }
                }
            }
        });
    }

    @Override
    public void unRegistry(NtsPlugin ntsPlugin) throws Exception {
        List<Class<?>> agentClasses = ntsPlugin.getClassList("agents");
        agentClasses.forEach(clazz -> {
            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                AgentMethod annotation = declaredMethod.getAnnotation(AgentMethod.class);
                if (annotation != null) {
                    Object o = ntsPlugin.getPluginApplicationContext().getDefaultListableBeanFactory().getBean(clazz);
                    ProxyMethodInteceptorFactory.removeProxyMethod(annotation.id(), o, declaredMethod);
                    logger.info("[{}]-[{}]-[{}] unregister agent method.", ntsPlugin.getPluginWrapper().getPluginId(),
                        clazz.getName(),declaredMethod.getName());
                }
            }
        });
    }
}
