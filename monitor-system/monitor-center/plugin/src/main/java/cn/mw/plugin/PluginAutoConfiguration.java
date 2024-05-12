/**
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
 *
 * 插件 加载和卸载的处理类 接口
 * @author qiyao(1210)
 * @date 2022-03-09
 */
package cn.mw.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import cn.mw.plugin.config.PluginBeanFactoryPostProcessor;
import cn.mw.plugin.register.ClassProcessor;
import cn.mw.plugin.register.DefaultPluginPipeProcessor;
import cn.mw.plugin.register.PluginRegistryWrapperContextHolder;
import cn.mw.plugin.register.ResourceLoaderProcessor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;


/**
 * @author： wjun_java@163.com
 * @date： 2021/4/27
 * @description：
 * @modifiedBy：
 * @version: 1.0
 */
@Configuration
@Import({PluginBeanFactoryPostProcessor.class})
@Slf4j
public class PluginAutoConfiguration implements EnvironmentAware, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public PluginAutoConfiguration() {
        //System.out.println("load PluginAutoConfiguration");
    }

    private Environment environment;

    private Path path;

    @Value("${mw.plugin.path:plugins}")
    public void setPath(String path) {
        this.path = Paths.get(path);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SpringPluginManager springPluginManager(){
        SpringPluginManager springPluginManager = new SpringPluginManager(path);
        springPluginManager.setApplicationContext(applicationContext);
        log.info("load start");
        springPluginManager.loadPlugins();
        log.info("load end");
        springPluginManager.startPlugins();
        log.info("start end");

        List<PluginPipeProcessor> pluginPipeProcessors = new ArrayList<>();
        DefaultPluginPipeProcessor defaultPluginPipeProcessor = new DefaultPluginPipeProcessor();
        defaultPluginPipeProcessor.setApplicationContext(applicationContext);
        pluginPipeProcessors.add(defaultPluginPipeProcessor);

        for(PluginWrapper pluginWrapper : springPluginManager.getPlugins()){
            log.info("{} class registry start" ,pluginWrapper.getPluginId());
            NtsPlugin ntsPlugin = new NtsPlugin(pluginWrapper, applicationContext);
            String require = pluginWrapper.getDescriptor().getRequires();
            try {
                for(PluginPipeProcessor pluginPipeProcessor : pluginPipeProcessors){
                    pluginPipeProcessor.initialize();
                    PluginRegistContext pluginRegistContext = new PluginRegistContext();
                    if(null != require){
                        pluginRegistContext.addValue(PluginRegistContext.RequireKey ,require);
                    }
                    pluginPipeProcessor.registry(ntsPlugin ,pluginRegistContext);
                }
                log.info("{} class registry end" ,pluginWrapper.getPluginId());
            } catch (Exception e) {
                log.error("springPluginManager" ,e);
            }
            PluginRegistryWrapperContextHolder.put(pluginWrapper.getPluginId(), ntsPlugin);
        }
        return springPluginManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
