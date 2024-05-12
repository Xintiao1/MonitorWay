package cn.mw.monitor.api.common;/**
 * Created by dev on 2020/3/12.
 */

import cn.mw.monitor.common.bean.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @ClassName SpringUtils
 * @Description TODO
 * @Author dev
 * @Date 2020/3/12 17:29
 * @Version 1.0
 **/
@Component
@Slf4j
public class SpringUtils implements ApplicationContextAware, InitializingBean {

    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringUtils.applicationContext == null){
            SpringUtils.applicationContext  = applicationContext;
        }
    }


    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz){
        return getApplicationContext().getBean(name, clazz);
    }
    
    /**
     * 根据类名获取到bean
     * @param <T>
     * @param clazz
     * @return
     * @throws BeansException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBeanByName(Class<T> clazz) throws BeansException {
        try {
            char[] cs=clazz.getSimpleName().toCharArray();
            cs[0] += 32;//首字母大写到小写
            return (T) applicationContext.getBean(String.valueOf(cs));
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
            return null;
        } 
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }

    public static String getPropertiesValue(String proName) {
        return applicationContext.getBean(PropertiesUtils.class).getPropertiesValue(proName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /*
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            ////System.out.println(">>>>>>" + name);
        }
        ////System.out.println("------\nBean 总计:" + applicationContext.getBeanDefinitionCount());

         */
    }
}

