package cn.mw.monitor.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MwLoginLog {
    String value() default "";
}
