package cn.mw.plugin.register;

import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.PluginPipeProcessor;
import cn.mw.plugin.PluginRegistContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


/**
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public class ControllerPluginPipeProcessor implements PluginPipeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ControllerPluginPipeProcessor.class);

    RequestMappingHandlerMapping requestMappingHandlerMapping;

    ApplicationContext applicationContext;

    Method getMappingForMethod;

    public ControllerPluginPipeProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize() {
        requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
        getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);
        getMappingForMethod.setAccessible(true);
    }

    @Override
    public void registry(NtsPlugin ntsPlugin ,PluginRegistContext pluginRegistContext) throws Exception {
        List<Class<?>> controllerClasses = ntsPlugin.getClassList("controller");
        controllerClasses.forEach(controllerClass -> {
            Object bean = ntsPlugin.getPluginApplicationContext().getBean(controllerClass);
            Method[] methods = controllerClass.getMethods();
            //不支持 DeleteMapping和PutMapping
            for (Method method : methods) {
                if (method.getAnnotation(RequestMapping.class) != null
                    || method.getAnnotation(GetMapping.class) != null
                    || method.getAnnotation(PostMapping.class) != null) {
                    RequestMappingInfo requestMappingInfo = null;
                    try {
                        requestMappingInfo = (RequestMappingInfo) getMappingForMethod.invoke(requestMappingHandlerMapping, method, controllerClass);
                        ntsPlugin.addPluginRequestMapping(requestMappingInfo);
                        // Class clazz = requestMappingHandlerMapping.getClass();
                        requestMappingHandlerMapping.registerMapping(requestMappingInfo, bean, method);
                        logger.info("ControllerPluginPipeProcessor-plugin-[{}] registerMapping method:[{}]", ntsPlugin.getPluginId(), method.getName());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void unRegistry(NtsPlugin ntsPlugin) throws Exception {
        //注销所有注册的Mapping
        ntsPlugin.getRequestMappingInfos().forEach(requestMappingInfo -> requestMappingHandlerMapping.unregisterMapping(requestMappingInfo));
    }
}
