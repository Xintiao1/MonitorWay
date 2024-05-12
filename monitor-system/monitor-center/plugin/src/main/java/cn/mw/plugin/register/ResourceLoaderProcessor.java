/**
 * Copyright (c) 广州小橘灯信息科技有限公司 2016-2017, wjun_java@163.com.
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * http://www.xjd2020.com
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.mw.plugin.register;

import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.PluginPipeProcessor;
import cn.mw.plugin.PluginRegistContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * 插件包内资源加载处理器.
 * 扫描 包内的 class文件, 文件资源， (mapper.xml, yaml配置文件, property资源)
 *
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public class ResourceLoaderProcessor implements PluginPipeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ResourceLoaderProcessor.class);

    @Override
    public void initialize() {

    }

    @Override
    public void registry(NtsPlugin ntsPlugin , PluginRegistContext pluginRegistContext) throws Exception {
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver(ntsPlugin.getPluginWrapper().getPluginClassLoader());
        String pluginBasePath = ClassUtils.classPackageAsResourcePath(ntsPlugin.getPluginWrapper().getPlugin().getClass());

        //扫描plugin所有的类class文件
        Resource[] classResources = pathMatchingResourcePatternResolver.getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + pluginBasePath + "/**/*.class");
        if (classResources.length > 0) {
            ntsPlugin.setClassResourceList(new ArrayList<>(Arrays.asList(classResources)));
        }

        String pluginName = ntsPlugin.getPluginPath().getFileName().toString();

        //扫描mybatis mapper.xml文件
        try {
            Resource[] mapperXmlResources = pathMatchingResourcePatternResolver.getResources("classpath*:**/*Mapper.xml");
            List<Resource> resources = new ArrayList<>();
            for (Resource mapperXmlResource : mapperXmlResources) {
                if (mapperXmlResource.getURL().toString().contains(pluginName)) {
                    resources.add(mapperXmlResource);
                }
            }
            if (resources.size() > 0) {
                logger.info("ResourceLoaderProcessor [{}] find Mapper.xml: {}",
                    ntsPlugin.getPluginId(), resources);
                ntsPlugin.setMapperXmlResourceList(resources);
            } else {
                logger.info("ResourceLoaderProcessor [{}] find no Mapper xml.",ntsPlugin.getPluginId());
                ntsPlugin.setMapperXmlResourceList(new ArrayList<>());
            }
        } catch (IOException e) {
            //System.out.println(e.getMessage());
            logger.warn("[" + ntsPlugin.getPluginId() + "] scan Mapper encounter a exception.", e);
            ntsPlugin.setMapperXmlResourceList(new ArrayList<>());
        }
    }

    @Override
    public void unRegistry(NtsPlugin ntsPlugin) throws Exception {
        ntsPlugin.getClassResourceList().clear();
        ntsPlugin.getMapperXmlResourceList().clear();
    }

}
