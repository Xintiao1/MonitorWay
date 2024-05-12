package cn.mw.plugin.register;

import cn.mw.plugin.NtsPlugin;
import cn.mw.plugin.PluginPipeProcessor;
import cn.mw.plugin.PluginRegistContext;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;


/**
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public class MapperPluginPipeProcessor implements PluginPipeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MapperPluginPipeProcessor.class);

    @Override
    public void initialize() {

    }

    @Override
    public void registry(NtsPlugin ntsPlugin ,PluginRegistContext pluginRegistContext) throws Exception {
        List<Class<?>> mapperClassList = ntsPlugin.getClassList("mapper");
        if (mapperClassList == null || mapperClassList.isEmpty()) {
            return;
        }

        String pluginClass = ntsPlugin.getPluginWrapper().getDescriptor().getPluginClass();
        int lastSep = pluginClass.lastIndexOf(".");
        String pluginPrefix = pluginClass.substring(0 ,lastSep) + ".monitor.dao";

        RuntimeBeanReference pluginRuntimeBeanReference = null;
        SqlSessionFactory pluginSqlSessionFactory = null;
        String pluginMapperDir = null;

        boolean hasPluginDataSource = false;
        String sqlSessionFactoryValue = pluginRegistContext.getValue(PluginRegistContext.RequireKey).toString();
        if(null != sqlSessionFactoryValue){
            String[] keyValue = sqlSessionFactoryValue.split(":");
            if("sqlSessionFactory".equals(keyValue[0])){
                hasPluginDataSource = true;
                pluginMapperDir = keyValue[2];
                pluginRuntimeBeanReference = new RuntimeBeanReference(keyValue[1]);
                pluginSqlSessionFactory = ntsPlugin.getMainApplicationContext().getBean(keyValue[1] ,SqlSessionFactory.class);
            }
        }

        RuntimeBeanReference runtimeBeanReference = new RuntimeBeanReference(SqlSessionFactory.class);
        SqlSessionFactory sqlSessionFactory = ntsPlugin.getMainApplicationContext().getBean(SqlSessionFactory.class);


        //注册mapper
        for (Class<?> mapperClass : mapperClassList) {
            GenericBeanDefinition definition = new GenericBeanDefinition();
            definition.getConstructorArgumentValues().addGenericArgumentValue(mapperClass);
            definition.setBeanClass(MapperFactoryBean.class);
            definition.getPropertyValues().add("addToConfig", true);

            String mapperClassPath = mapperClass.getName();
            if(hasPluginDataSource && mapperClassPath.indexOf(pluginPrefix) < 0){
                definition.getPropertyValues().add("sqlSessionFactory" ,pluginRuntimeBeanReference);
            }else {
                definition.getPropertyValues().add("sqlSessionFactory", runtimeBeanReference);
            }

            //definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            ntsPlugin.getPluginApplicationContext().registerBeanDefinition(mapperClass.getName(), definition);
            logger.info("MapperPluginPipeProcessor-plugin-[{}]  mybatis bean:[{}] instantiation.", ntsPlugin.getPluginId(), mapperClass.getName());
        }

        //注册mapper.xml
        try {
            Resources.setDefaultClassLoader(ntsPlugin.getPluginWrapper().getPluginClassLoader());
            for (Resource mapperXmlResource : ntsPlugin.getMapperXmlResourceList()) {
                if (mapperXmlResource == null) {
                    continue;
                }

                if (!Objects.requireNonNull(mapperXmlResource.getFilename()).endsWith("Mapper.xml")) {
                    continue;
                }

                try {
                    Configuration configuration = null;
                    if(hasPluginDataSource && mapperXmlResource.getURL().getPath().indexOf(pluginMapperDir) >= 0){
                        configuration = pluginSqlSessionFactory.getConfiguration();
                    }else{
                        configuration = sqlSessionFactory.getConfiguration();
                    }

                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperXmlResource.getInputStream(),
                                configuration, mapperXmlResource.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                    logger.info("MapperPluginPipeProcessor-plugin-[{}]  mybatis mapper-xml:[{}] registered.", ntsPlugin.getPluginId(), mapperXmlResource.getFilename());
                } catch (Exception e) {
                    throw new NestedIOException("Failed to parse mapping resource: '" + mapperXmlResource + "'", e);
                } finally {
                    ErrorContext.instance().reset();
                }
            }
        } finally {
            Resources.setDefaultClassLoader(ClassUtils.getDefaultClassLoader());
        }

    }

    @Override
    public void unRegistry(NtsPlugin ntsPlugin) throws Exception {
        List<Class<?>> mapperClassList = ntsPlugin.getClassList("mapper");
        if (mapperClassList.isEmpty()) {
            return;
        }

        //卸载mapper
        //SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) ntsPlugin.getMainApplicationContext().getBean("sqlSessionFactory");
        //Configuration configuration = sqlSessionFactory.getConfiguration();
        for (Class<?> baseMapperClass : mapperClassList) {
            ntsPlugin.getPluginApplicationContext().removeBeanDefinition(baseMapperClass.getName());
        }
    }

}
