package cn.mw;

import cn.mw.monitor.common.constant.Constants;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.ibatis.executor.Executor;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.*;

/**
 * @author lumingming
 * @createTime 2023309 10:30
 * @description
 */
//在mybatis中可被拦截的类型有四种(按照拦截顺序)：
//
//        Executor: 拦截执行器的方法。
//        ParameterHandler: 拦截参数的处理。
//        ResultHandler：拦截结果集的处理。
//        StatementHandler: 拦截Sql语法构建的处理。
//@Intercepts({
//    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
//    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
//    @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})
//})
//	1. @Intercepts：标识该类是一个拦截器；
//      @Signature：指明自定义拦截器需要拦截哪一个类型，哪一个方法；
//    2.1 type：对应四种类型中的一种；
//    2.2 method：对应接口中的哪类方法（因为可能存在重载方法）；
//    2.3 args：对应哪一个方法；
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}),
})
@Slf4j
@Component  // 必须要交给 spring boot 管理
public class MybatisInterceptor implements Interceptor {

    private static String DELEGATE_STRING = "delegate.mappedStatement";

    private static String STRING_SQL = "sql";

    private final static String SEPARATOR = ",";

    private final static String BLANK = " ";

    private final static String DUAL = "dual";

    private final static Set<String> KEYWORD_SET = new HashSet<>();

    private final static Set<String> END_WITH_SET = new HashSet<>();

    @Value("${datasource.check}")
    private String check;

    //这个方法里 是重点  主要是拦截 需要执行的sql
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //oracle数据库才去拦截
        if (Constants.DATABASE_ORACLE.equalsIgnoreCase(check)) {
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

            // 通过MetaObject优雅访问对象的属性，这里是访问statementHandler的属性;：MetaObject是Mybatis提供的一个用于方便、
            // 优雅访问对象属性的对象，通过它可以简化代码、不需要try/catch各种reflect异常，同时它支持对JavaBean、Collection、Map三种类型对象的操作。
            //实际执行的sql是经过层层封装，无法利用简单的一层反射获取到需要使用提供的快捷方法或者对获取到关键数据进行拼装
            MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                    new DefaultReflectorFactory());

            // 先拦截到RoutingStatementHandler，里面有个StatementHandler类型的delegate变量，其实现类是BaseStatementHandler，然后就到BaseStatementHandler的成员变量mappedStatement
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(DELEGATE_STRING);

            // id为执行的mapper方法的全路径名，如com.cq.UserMapper.insertUser， 便于后续使用反射
            String id = mappedStatement.getId();
            // sql语句类型 select、delete、insert、update
            String sqlCommandType = mappedStatement.getSqlCommandType().toString();

            BoundSql boundSql = statementHandler.getBoundSql();
            // 获取到原始sql语句
            String sql = boundSql.getSql();
            // 数据库连接信息
            if (JudgeCase(sql)) {
                String msql = changeMsqy(sql);
                Field field = boundSql.getClass().getDeclaredField(STRING_SQL);
                field.setAccessible(true);
                field.set(boundSql, msql);
            }


            // 增强sql
            // 通过反射，拦截方法上带有自定义@SqlPermission，并增强sql
            //离谱的是之前的反射无法生效，不知道为什么这个可以生效有待研究
            /*     String mSql = sqlAnnotationEnhance(id, sqlCommandType, sql);*/
            // 直接增强sql
            //通过反射修改sql语句
        /*Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, mSql);
        log.info("增强后的SQL：{}", mSql);*/ // 打印：增强后的SQL：select * from scenario_storage limit 2
        }
        return invocation.proceed();
    }

    private boolean JudgeCase(String sql) {
        if (sql.contains("\"") || sql.endsWith(DUAL)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * ORACLE拦截SQL，将所有字段
     *
     * @param sql
     * @return
     */
    private String changeMsqy(String sql) {
        List<String> list = Arrays.asList(sql.split(" |\n"));
        StringBuffer newSql = new StringBuffer();
        for (String str : list) {
            if (StringUtils.isNotBlank(str)) {
                boolean isEnd = str.lastIndexOf(SEPARATOR) == str.length() - 1;
                //如果columns是用，拼接在一起
                if (str.contains(SEPARATOR)) {
                    List<String> columnList = Arrays.asList(str.split(SEPARATOR));
                    boolean isFirst = true;
                    for (String column : columnList) {
                        column = checkUpOrDown(column);
                        if (isFirst) {
                            newSql.append(BLANK).append(column);
                            isFirst = false;
                        } else {
                            newSql.append(SEPARATOR).append(column);
                        }
                    }
                    //如果最后一位是，则需要补充
                    if (isEnd) {
                        newSql.append(SEPARATOR);
                    }
                } else {
                    str = checkUpOrDown(str);
                    newSql.append(BLANK).append(str);
                }
            }
        }
        return newSql.toString();
    }

    /**
     * 判断是否需要添加""
     *
     * @param str
     * @return
     */
    private String checkUpOrDown(String str) {
        if (str.equals(str.toLowerCase())) {
            //如果是纯数字或者是以()包裹的，直接返回
            if (str.matches("\\d+") || str.matches("^\\(.*\\)$")){
                return str;
            }
            //判断是否为关键字或者关键词
            if (checkKeyWord(str)) {
                return str;
            }
            String end = checkEndWith(str);
            if (StringUtils.isNotEmpty(end)) {
                str = str.substring(0, str.lastIndexOf(end));
                return "\"" + str + "\"" + BLANK + end;
            }
            return "\"" + str + "\"";
        } else {
            return str;
        }
    }

    /**
     * 判断是否由这些结束
     *
     * @param str
     * @return
     */
    private String checkEndWith(String str) {
        for (String end : END_WITH_SET) {
            if (str.endsWith(end)) {
                return end;
            }
        }
        return null;
    }

    private boolean checkKeyWord(String str) {
        return KEYWORD_SET.contains(str);
    }


    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties prop) {

    }

    /**
     * <p>
     * 获得真正的处理对象,可能多层代理.
     * </p>
     */
    @SuppressWarnings("unchecked")
    public static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }

    static {
        KEYWORD_SET.add(">");
        KEYWORD_SET.add("<");
        KEYWORD_SET.add("=");
        KEYWORD_SET.add("!=");
        KEYWORD_SET.add("<>");
        KEYWORD_SET.add(">=");
        KEYWORD_SET.add("<=");
        KEYWORD_SET.add("count(0)");
        KEYWORD_SET.add("?");
        KEYWORD_SET.add("*");
        KEYWORD_SET.add("(");
        KEYWORD_SET.add(")");
        KEYWORD_SET.add("as");
        KEYWORD_SET.add("max");
        KEYWORD_SET.add("max(");
        KEYWORD_SET.add("COUNT(");
        KEYWORD_SET.add("count(");
        KEYWORD_SET.add("(?");
        KEYWORD_SET.add("?)");
        END_WITH_SET.add("=?");
//        END_WITH_SET.add("dual");
    }
}

