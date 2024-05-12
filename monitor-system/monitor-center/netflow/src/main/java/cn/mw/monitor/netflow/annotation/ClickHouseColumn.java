package cn.mw.monitor.netflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author guiquanwnag
 * @datetime 2023/6/19
 * @Description 自定义clickhouse的属性注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ClickHouseColumn {

    /**
     * ClickHouse列名
     *
     * @return
     */
    String name() default "";

    /**
     * ClickHouse数据类型
     *
     * @return
     */
    String type() default "String";

    /**
     * 是否为主键
     *
     * @return
     */
    boolean isPrimaryKey() default false;

}
