package cn.mw.monitor.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD,ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MwProcess {
    //bean名称,springUtils通过这个获取对应bean对象
    String beanName() default "";

    //绑定的模型名称
    String moduleName() default "";

    //对模型新增或删除等操作
    String action() default "";
}
