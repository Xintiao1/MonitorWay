package cn.mw.monitor.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD,ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MwPermit {
    String moduleName() default "";
}
