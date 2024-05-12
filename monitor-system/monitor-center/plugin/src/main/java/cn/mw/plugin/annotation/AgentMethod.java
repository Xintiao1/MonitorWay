package cn.mw.plugin.annotation;

import java.lang.annotation.*;

/**
 * 插件中实现代理的方法标定 注解.
 * @author lijubo(1210)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentMethod {
    String id() default "";
}
