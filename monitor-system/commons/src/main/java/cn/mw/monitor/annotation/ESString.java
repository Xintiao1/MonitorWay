package cn.mw.monitor.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESString {
    boolean hasKeyword() default true;
}
