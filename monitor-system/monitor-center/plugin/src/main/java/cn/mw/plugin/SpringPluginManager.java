/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.mw.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import cn.mw.plugin.entity.PluginInfo;
import cn.mw.plugin.pf4j.NtsJarPluginLoader;
import cn.mw.plugin.register.PluginRegistryWrapperContextHolder;
import org.pf4j.CompoundPluginLoader;
import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginManager;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.DevelopmentPluginRepository;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginLoader;
import org.pf4j.PluginRepository;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;


/**
 *
 */
public class SpringPluginManager extends DefaultPluginManager implements PluginManagerService {
    private static final Logger logger = LoggerFactory.getLogger(SpringPluginManager.class);

    private GenericApplicationContext applicationContext;

    private PluginPipeProcessor pluginPipeProcessor;

    private boolean initPluginsFlag = false;

    public SpringPluginManager() {
        super();
    }

    public SpringPluginManager(Path... pluginsRoots) {
        super(pluginsRoots);
    }

    public SpringPluginManager(List<Path> pluginsRoots) {
        super(pluginsRoots);
    }

    public void setPluginPipeProcessor(PluginPipeProcessor pluginPipeProcessor) {
        this.pluginPipeProcessor = pluginPipeProcessor;
    }

    @Override
    protected PluginLoader createPluginLoader() {
        return (new CompoundPluginLoader())
            .add(new DevelopmentPluginLoader(this), this::isDevelopment)
            .add(new NtsJarPluginLoader(this));
    }

    @Override
    protected PluginRepository createPluginRepository() {
        return (new CompoundPluginRepository())
            .add(new DevelopmentPluginRepository(this.getPluginsRoots()), this::isDevelopment)
            .add(new JarPluginRepository(this.getPluginsRoots()))
            .add(new DefaultPluginRepository(this.getPluginsRoots()), this::isNotDevelopment);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (GenericApplicationContext) applicationContext;

    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * This method load, start plugins and inject extensions in Spring
     */
    // @PostConstruct
    public void init() {
        //插件加载
        loadPlugins();
    }

    @Override
    public void initPlugins() throws Exception {
        if (initPluginsFlag) {
            return;
        }
        //初始化 处理器
        pluginPipeProcessor.initialize();

        for (PluginWrapper startedPlugin : getPlugins()) {
            startSpringPlugin(startedPlugin.getPluginId());
        }
        initPluginsFlag = true;
    }

    @Override
    public String installSpringPlugin(Path path) throws Exception {
        try {
            logger.info("SpringPluginManager-installSpringPlugin-path[{}] begin", path.getFileName());
            String pluginId = loadPlugin(path);
            logger.info("SpringPluginManager-installSpringPlugin-load-plugin id[{}] success", pluginId);
            return pluginId;
        } catch (Exception e) {
            logger.warn("SpringPluginManager-installSpringPlugin-load-plugin failed.", e);
        }
        return "error";
    }

    @Override
    public boolean startSpringPlugin(String pluginId) throws Exception {
        logger.info("SpringPluginManager-start plugin [{}] begin", pluginId);
        PluginWrapper pluginWrapper = getPlugin(pluginId);
        String require = pluginWrapper.getDescriptor().getRequires();
        PluginRegistContext pluginRegistContext = new PluginRegistContext();
        if(null != require){
            pluginRegistContext.addValue(PluginRegistContext.RequireKey ,require);
        }

        if (null == pluginWrapper) {
            PluginInfo pluginInfo = PluginRegistryWrapperContextHolder.getPluginInfo(pluginId);
            if (null == pluginInfo) {
                logger.info("SpringPluginManager-plugin [{}] not exist.", pluginId);
                return false;
            }
            pluginWrapper = getPlugin(loadPlugin(Paths.get(pluginInfo.getPluginPath())));
        }
        startPlugin(pluginId);
        NtsPlugin ntsPlugin = PluginRegistryWrapperContextHolder.getPluginRegistryWrapper(pluginId);
        if (null == ntsPlugin) {
            ntsPlugin = new NtsPlugin(pluginWrapper, applicationContext);
            try {
                pluginPipeProcessor.registry(ntsPlugin ,pluginRegistContext);
                PluginRegistryWrapperContextHolder.put(pluginId, ntsPlugin);
                logger.info("SpringPluginManager-start plugin [{}] success.", ntsPlugin.getPluginId());
                return true;
            } catch (Exception e) {
                ntsPlugin.unload();
                PluginRegistryWrapperContextHolder.remove(pluginId, true);
                logger.warn("SpringPluginManager-encounter exception[{}-{}]start plugin failed, remove the plugin[{}]",
                    e.getClass().getName(), e.getMessage(), ntsPlugin.getPluginId());
            }
        }
        //logger.info("nts plugin [{}] already start", pluginId);
        return true;
    }

    @Override
    public void stopSpringPlugin(String pluginId, boolean unload) throws Exception {
        logger.info("SpringPluginManager-stop plugin [{}] begin, unload:[{}]", pluginId, unload);
        NtsPlugin wrapper = PluginRegistryWrapperContextHolder.getPluginRegistryWrapper(pluginId);
        if (null != wrapper) {
            try {
                pluginPipeProcessor.unRegistry(wrapper);
                PluginRegistryWrapperContextHolder.remove(pluginId, unload);
            } catch (Exception e) {
                logger.warn("SpringPluginManager-stopSpringPlugin-[" + pluginId + "]-failed.", e);
                return;
            }

        }
        if (unload) {
            unloadPlugin(pluginId);
            logger.info("SpringPluginManager-unload plugin [{}] success", pluginId);
        } else {
            stopPlugin(pluginId);
            logger.info("SpringPluginManager-stop plugin [{}] success", pluginId);
        }
    }

    @Override
    public List<PluginInfo> getInstallSpringPlugins() {
        return PluginRegistryWrapperContextHolder.getPluginStatusList();
    }
}
