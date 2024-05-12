package cn.mw.plugin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.util.Arrays;

public class PluginBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PluginBeanFactoryPostProcessor.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        logger.debug("PluginBeanFactoryPostProcessor total bean count: {}", beanDefinitionRegistry.getBeanDefinitionCount());
        String[] beanDefintionNames = beanDefinitionRegistry.getBeanDefinitionNames();
        logger.debug("bean names: {}", Arrays.toString(beanDefintionNames));
        // for (String beanDefinitonName : beanDefintionNames) {
        //     //BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanDefinitonName);
        //     //System.out.println("key:[" + beanDefinition.getAttribute("key") + "],name: " + beanDefinitonName);
        //
        // }
        // try {
        //     BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition("org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration");
        //     beanDefinitionRegistry.removeBeanDefinition("org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration");
        //     beanDefinition.setBeanClassName("com.tiansu.nts.pluginPluginFrameworkSchedulingConfiguration");
        //
        // } catch (NoSuchBeanDefinitionException e) {
        //
        // }

        //
        // ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(beanDefinitionRegistry);
        // scanner.scan();
        // scanner.
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
