package cn.mw.monitor.netflow.service.clickhouse;

import cn.mw.monitor.netflow.annotation.ClickHouseColumn;
import cn.mw.monitor.netflow.constant.FlowConstant;
import cn.mw.monitor.netflow.entity.IP;
import cn.mw.monitor.netflow.enums.CKFieldType;
import cn.mwpaas.common.utils.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guiquanwnag
 * @datetime 2023/7/19
 * @Description clickhouse的操作工具类
 */
@Component
@Slf4j
public class ClickhouseUtil {


    private final DataSource dataSource;


    /**
     * 根据类获取clickhouse数据
     *
     * @param clazz     类名
     * @param execSql sql
     * @return
     */
    public <T> List<T> selectAll(String execSql, Class<T> clazz) {
        return this.selectAll(execSql, null, clazz);
    }

    /**
     * 根据类获取clickhouse数据
     *
     * @param clazz       类名
     * @param execSql   表名（需要加上库名，以db.table的格式传递）
     * @param whereClause where查询条件
     * @return
     */
    public <T> List<T> selectAll(String execSql, String whereClause, Class<T> clazz) {
        ResultSet resultSet = null;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            String sql;
            if (StringUtils.isEmpty(whereClause)) {
                sql = execSql;
            } else {
                sql = execSql + FlowConstant.SPACE + " where " + whereClause;
            }
            resultSet = statement.executeQuery(sql);
            List<T> resultList = new ArrayList<>();
            while (resultSet.next()) {
                T entity = clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object value = resultSet.getObject(fieldName, field.getType());
                    field.set(entity, value);
                }
                resultList.add(entity);
            }
            return resultList;
        } catch (Exception e) {
            log.error("获取clickhouse数据失败", e);
            return null;
        }
    }


    /**
     * 根据类获取clickhouse数据
     *
     * @param clazz   类名
     * @param execSql 执行sql
     * @return
     */
    public <T> List<T> selectAllData(String execSql, Class<T> clazz) {
        ResultSet resultSet = null;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            resultSet = statement.executeQuery(execSql);
            List<T> resultList = new ArrayList<>();
            ClickHouseColumn column;
            CKFieldType fieldType;
            while (resultSet.next()) {
                T entity = clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    //只有做了注解才会插入数据
                    if (field.isAnnotationPresent(ClickHouseColumn.class)) {
                        column = field.getAnnotation(ClickHouseColumn.class);
                        fieldType = CKFieldType.getByType(column.type());
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        Object value = null;
                        try {
                            if (IP.class == field.getType()) {
                                value = resultSet.getObject(fieldName, fieldType.getClazz());
                                value = new IP(String.valueOf(value));
                            }else {
                                value = resultSet.getObject(fieldName, field.getType());
                            }
                        } catch (SQLException e) {
                            //如果获取失败，则直接执行下一个
                            continue;
                        }
                        field.set(entity, value);
                    }
                }
                resultList.add(entity);
            }
            return resultList;
        } catch (Exception e) {
            log.error("获取clickhouse数据失败", e);
            return null;
        }
    }

    /**
     * 查询数据总数
     *
     * @param execSql 待执行SQL
     * @return
     */
    public long countData(String execSql) {
        long count = 0;
        ResultSet resultSet = null;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            resultSet = statement.executeQuery(execSql);
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
        } catch (Exception e) {
            log.error("获取clickhouse数据失败", e);
        }
        return count;
    }


    /**
     * 清空表数据(该操作不可逆)
     *
     * @param tableName 表名（需要加上库名，以db.table的格式传递）
     */
    public void truncateTable(String tableName) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            String sql = "TRUNCATE TABLE " + tableName;
            statement.executeUpdate(sql);
        } catch (Exception e) {
            log.error("clickhouse truncateTable error", e);
        }
    }

    @Autowired
    private ClickhouseUtil(@Qualifier("clickhouseDataSource") DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;

    }

    @SneakyThrows
    private Connection getConnection() {
        return this.dataSource.getConnection();
    }
}
