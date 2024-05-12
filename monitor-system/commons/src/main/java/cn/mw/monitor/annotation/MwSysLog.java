package cn.mw.monitor.annotation;

import java.lang.annotation.*;

/**
 * 系统日志注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MwSysLog {
    String value() default "";
    int type() default 0;
}

