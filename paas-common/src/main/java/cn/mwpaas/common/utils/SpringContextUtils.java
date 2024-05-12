package cn.mwpaas.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Created by linjian
 * 16/9/8.
 * 多线程中获取bean
 */
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext context = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 获取bean
     * @param beanName
     * @param <T>
     * @return
     */
    public static <T> T getBean(String beanName) {
        return (T) context.getBean(beanName);
    }

    /**
     * 根据类型获取bean
     * @param requiredType
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static  <T> T getBean(Class<T> requiredType) throws BeansException {
        return context.getBean(requiredType);
    }

    /**
     * 获取message
     * @param key
     * @return
     */
    public static String getMessage(String key) {
        return context.getMessage(key, null, Locale.getDefault());
    }

    /**
     * 获取properties
     * @param key
     * @return
     */
    public static String getProperties(String key) {
        return context.getEnvironment().getProperty(key);
    }
}
