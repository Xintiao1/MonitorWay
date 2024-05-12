package cn.mw.plugin.register;

import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.PluginPipeProcessor;
import cn.mw.plugin.PluginRegistContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public class DefaultPluginPipeProcessor implements PluginPipeProcessor, ApplicationContextAware {

    ApplicationContext applicationContext;
    //默认第一个加载
    //资源 分类加载处理器
    ResourceLoaderProcessor resourceLoaderProcessor = new ResourceLoaderProcessor();
    //类 分类实例化处理器
    ClassProcessor classProcessor = new ClassProcessor();

    List<PluginPipeProcessor> pluginPipeProcessorList = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void initialize() {
        classProcessor.initialize(applicationContext);
        resourceLoaderProcessor.initialize();
        //注册spring bean
        pluginPipeProcessorList.add(new SpringBeanProcessor());
        //注册 mybatis mapper
        pluginPipeProcessorList.add(new MapperPluginPipeProcessor());
        //context refresh
        pluginPipeProcessorList.add(new ApplicationContextProcessor());
        //
        pluginPipeProcessorList.add(new AgentRegisterProcessor());
        //注册 controller
        pluginPipeProcessorList.add(new ControllerPluginPipeProcessor(this.applicationContext));
        //检查 websocket
        pluginPipeProcessorList.add(new WebsocketProcessor());
        //pluginPipeProcessorList.add(new InterceptorPluginRegister(this.applicationContext));
        for (PluginPipeProcessor pluginPipeProcessor : pluginPipeProcessorList) {
            pluginPipeProcessor.initialize();
        }
    }

    @Override
    public void registry(NtsPlugin ntsPlugin ,PluginRegistContext pluginRegistContext) throws Exception {
        resourceLoaderProcessor.registry(ntsPlugin ,pluginRegistContext);
        classProcessor.registry(ntsPlugin ,pluginRegistContext);
        for (PluginPipeProcessor pluginPipeProcessor : pluginPipeProcessorList) {
            pluginPipeProcessor.registry(ntsPlugin ,pluginRegistContext);
        }
    }

    @Override
    public void unRegistry(NtsPlugin ntsPlugin) throws Exception {
        try {
            for (int i = pluginPipeProcessorList.size() - 1; i >= 0; i--) {
                pluginPipeProcessorList.get(i).unRegistry(ntsPlugin);
            }
            classProcessor.unRegistry(ntsPlugin);
            resourceLoaderProcessor.unRegistry(ntsPlugin);
        } finally {
            ntsPlugin.unload();
            //PluginRegistryWrapperContextHolder.remove(ntsPlugin.getPluginWrapper().getPluginId());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
