package cn.mw.plugin.annotation;

import java.lang.annotation.*;

/**
 * 被代理的方法标定 注解.
 * 插件对基线版本中定义的功能进行，功能替换或者增量
 * @author lijubo(1210)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProxyMethod {
    String id() default "";
}
