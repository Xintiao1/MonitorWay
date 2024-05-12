package cn.mw.syslog.common;

import cn.mw.monitor.common.constant.Constants;
import cn.mw.syslog.dto.LogTypeEnum;
import cn.mw.syslog.utils.GenerationTableName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.Calendar;

@Slf4j
@Component
@EnableScheduling
public class MwCreateLogTable  {

    @Value("${spring.datasource.driverClassName}")
    private String driver;
    @Value("${spring.datasource.username3}")
    private String userName;
    @Value("${spring.datasource.passwd3}")
    private String passwd;
    @Value("${spring.datasource.url3}")
    private String url;

    @Value("${datasource.check}")
    private String check;

    @Value("${orldatabase.username3}")
    private String oracleUsername;

    @Value("${orldatabase.passwd3}")
    private String oraclePassword;

    @Value("${orldatabase.driverClassName}")
    private String oracleDriverClassName;

    @Value("${orldatabase.url3}")
    private String oracledbUrl;

    @Scheduled(cron = "0 0 0 1 1,4,7,10 ?")
    public void init() {
        Connection conn = null;
        Statement stat = null;
        ResultSet res = null;
        String driverParam = driver;
        String userNameParam=userName;
        String passwdParam=passwd;
        String urlParam=url;

        if (check.equals(Constants.DATABASE_ORACLE)){
             driverParam = oracleDriverClassName;
             userNameParam=oracleUsername;
             passwdParam=oraclePassword;
             urlParam=oracledbUrl;
        }
        try {
            Calendar now = Calendar.getInstance();
            String ltn = GenerationTableName.getTableNameByType(now, LogTypeEnum.LOGINTABLENAME.getName());
            Class.forName(driverParam);
            conn = DriverManager.getConnection(urlParam, userNameParam, passwdParam);
            stat = conn.createStatement();
            res = conn.getMetaData().getTables(null,null, ltn,null);
            createLoginTable(res, stat, ltn);

            //系统日志表创建
            String stn=GenerationTableName.getTableNameByType(now,LogTypeEnum.SYSTABLENAME.getName());
            res = conn.getMetaData().getTables(null,null, stn,null);
            createSystemTable(res,stat,stn);
        } catch (ClassNotFoundException e) {
            log.error("MySQL 驱动加载异常！", e);
        } catch (SQLException e) {
            log.error("MySQL 连接异常！error:", e);
        } finally {
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) {
                    log.error("记录集ResultSet关闭异常！", e);
                }
            }
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    log.error("声明Statement关闭异常！", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("连接对象Connection关闭异常！", e);
                }
            }
        }
    }


    //登录日志表
    private void createLoginTable(ResultSet resultSet, Statement stat, String ltn) throws SQLException {
        if (resultSet.next()) {
            log.info("数据库中已存在该表！");
        } else {
            String table = "";
            if (check.equals(Constants.DATABASE_ORACLE)){
                table ="CREATE TABLE \""+ltn+"\" (\n" +
                        "  \"id\" NUMBER(20,0) VISIBLE NOT NULL,\n" +
                        "  \"log_time\" DATE VISIBLE NOT NULL,\n" +
                        "  \"user_name\" NVARCHAR2(50) VISIBLE,\n" +
                        "  \"model_name\" NVARCHAR2(50) VISIBLE,\n" +
                        "  \"obj_name\" NCLOB VISIBLE,\n" +
                        "  \"operate_des\" NCLOB VISIBLE,\n" +
                        "  \"type\" NVARCHAR2(125) VISIBLE\n" +
                        ");";
            }else {
                 table = "CREATE TABLE `" + ltn + "` ("
                        + "`log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增序列',"
                        + "`user_ip` varchar(50) DEFAULT NULL COMMENT '登录者IP',"
                        + "`user_name` varchar(50) DEFAULT NULL COMMENT '系统登录用户名',"
                        + "`create_date` datetime DEFAULT NULL COMMENT '登录时间',"
                        +"`is_success` varchar(50) DEFAULT NULL COMMENT '是否成功',"
                        +"`login_way` varchar(50) DEFAULT NULL COMMENT '登录方式',"
                        +"`fail_type` varchar(50) DEFAULT NULL COMMENT '失败原因',"
                        + "PRIMARY KEY (`log_id`) "
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            }

            stat.executeUpdate(table);
            if (check.equals(Constants.DATABASE_ORACLE)){
                String sql1 ="ALTER TABLE \""+ltn+"\" ADD CONSTRAINT \"SYS_C0012469\" CHECK (\"id\" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;\n";
                stat.executeUpdate(sql1);
                String sql2 ="ALTER TABLE \""+ltn+"\" ADD CONSTRAINT \"SYS_C0012470\" CHECK (\"log_time\" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;\n";
                stat.executeUpdate(sql2);
            }
            log.info(ltn + "已成功创建！");
        }
    }

    //系统日志表
    private void createSystemTable(ResultSet resultSet, Statement stat, String stn) throws SQLException {
        if (resultSet.next()) {
            log.info("数据库中已存在该表！");
        } else {
            String table ="";

            if (check.equals(Constants.DATABASE_ORACLE)){
                table ="CREATE TABLE \""+stn+"\" (\n" +
                        "  \"log_id\" NUMBER(20,0) VISIBLE NOT NULL,\n" +
                        "  \"user_ip\" NVARCHAR2(50) VISIBLE,\n" +
                        "  \"user_name\" NVARCHAR2(50) VISIBLE,\n" +
                        "  \"create_date\" DATE VISIBLE,\n" +
                        "  \"is_success\" NVARCHAR2(50) VISIBLE,\n" +
                        "  \"login_way\" NVARCHAR2(50) VISIBLE,\n" +
                        "  \"fail_type\" NVARCHAR2(50) VISIBLE\n" +
                        ");";
            }else {
                table = "CREATE TABLE `" + stn + "` ("
                        + "`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增序列',"
                        + "`log_time` datetime(6) NOT NULL COMMENT '创建时间',"
                        + "`user_name` varchar(50) DEFAULT NULL COMMENT '用户名',"
                        + "`model_name` varchar(50) DEFAULT NULL COMMENT '模块名称',"
                        + "`obj_name` varchar(3000) DEFAULT NULL COMMENT '操作对象',"
                        + "`operate_des` varchar(3000) DEFAULT NULL COMMENT '操作描述',"
                        + "`type` varchar(125) DEFAULT NULL COMMENT '模块类型',"
                        + "PRIMARY KEY (`id`) "
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            }
            stat.executeUpdate(table);

            if (check.equals(Constants.DATABASE_ORACLE)){
                String sql1 ="ALTER TABLE \""+stn+"\" ADD CONSTRAINT \"SYS_C0012440\" PRIMARY KEY (\"log_id\");";
                stat.executeUpdate(sql1);
                String sql2 ="ALTER TABLE \""+stn+"\" ADD CONSTRAINT \"SYS_C0012387\" CHECK (\"log_id\" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE;";
                stat.executeUpdate(sql2);
            }
            log.info(stn + "已成功创建！");
        }
    }


    private void createTable(ResultSet resultSet, Statement stat, String tn) throws SQLException {
        if (resultSet.next()) {
            log.info("数据库中已存在该表！");
        } else {
            String table = "CREATE TABLE `" + tn + "` ("
                    + "`log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增序列',"
                    + "`remark` varchar(128) NOT NULL COMMENT '日志注解备注',"
                    + "`user_name` varchar(128) NOT NULL COMMENT '方法调用者',"
                    + "`user_ip` varchar(50) NOT NULL COMMENT '调用者IP',"
                    + "`class_name` varchar(255) NOT NULL COMMENT '调用类名称',"
                    + "`method_name` varchar(255) NOT NULL COMMENT '调用方法名称',"
                    + "`type` int(11) NOT NULL COMMENT '日志类型：0.系统日志 1.登录日志 2.执行日志',"
                    + "`exeu_time` int(11) NOT NULL COMMENT '方法调用耗时',"
                    + "`create_date` datetime NOT NULL COMMENT '方法调用时间',"
                    + "PRIMARY KEY (`log_id`) USING BTREE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            stat.executeUpdate(table);
            log.info(tn + "已成功创建！");
        }
    }

}
