package cn.mw.monitor.dbinit.test;

import cn.mw.monitor.dbinit.entiy.SqlTable;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.*;
import java.util.List;
@Slf4j
public class JdbcDri {
    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://10.18.5.30:3306/monitor_lic_test?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=GMT";
    static  final String path = "DbSqlUpdate.txt";


    // 数据库的用户名与密码，最高权限的账号
    static final String USER = "mwdevsql";
    static final String PASS = "Dev20$uiyD7";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
            /*json.string*/
            // 打开链接
         /*   //System.out.println("连接数据库...");*/
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // 执行查询
            /*//System.out.println(" 实例化Statement对象...");*/
            stmt = conn.createStatement();

            List<SqlTable> sqlTables = SqlTableUntil.getSqlTable(stmt);
//            String s =  SqlTableUntil.getCreateSql(stmt,"ACT_EVT_LOG");
            String s = JSON.toJSONString(sqlTables);
            clearInfoForFile(path);
            writeText(path,s);
            String newS = txt2String(path);
            List<SqlTable> newSql = JSON.parseArray(newS, SqlTable.class);
            /*List<String> sql = SqlContrast.Contrast(sqlTables,newSql);*/
         /*   List<BackReturn> backReturn = SqlTableUntil.UpdateSql(stmt,sql);*/
            /*测试*/
           /* List<String> sql = new ArrayList<>();
            sql.add("CREATE TABLE `ACT_EVT_LOG_copy1`  (\n" +
                    "  `LOG_NR_` bigint NOT NULL AUTO_INCREMENT,\n" +
                    "  `TYPE_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,\n" +
                    "  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,\n" +
                    "  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,\n" +
                    "  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,\n" +
                    "  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,\n" +
                    "  `TIME_STAMP_` timestamp NOT NULL DEFAULT current_timestamp ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "  `USER_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,\n" +
                    "  `DATA_` longblob NULL,\n" +
                    "  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,\n" +
                    "  `LOCK_TIME_` timestamp NULL DEFAULT NULL,\n" +
                    "  `IS_PROCESSED_` tinyint NULL DEFAULT 0,\n" +
                    "  PRIMARY KEY (`LOG_NR_`) USING BTREE\n" +
                    ") ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = DYNAMIC;\n");
            List<BackReturn> backReturn = SqlTableUntil.UpdateSql(stmt,sql);*/

            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            log.error("错误信息："+se);
        }catch(Exception e){
            // 处理 Class.forName 错误
            log.error("错误信息："+e);
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                log.error("错误信息："+se);
            }
        }
     /*   //System.out.println("Goodbye!");*/
    }


    public static void writeText(String path, String text) {
        try {
            FileOutputStream o = new FileOutputStream(path);

            o.write(text.getBytes("UTF-8"));

            o.close();

        } catch(Exception e) {}

    }

    public static String txt2String(String path) throws FileNotFoundException {
        File file = new File(path);/*文件名*/
       /* File file = ResourceUtils.getFile("classpath:"+path);*/
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(System.lineSeparator()+s);
            }
            br.close();
        }catch(Exception e){
            log.error("错误信息："+e);

        }
        return result.toString();
    }

    public static void clearInfoForFile(String path) {
        File file = new File(path);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            log.error("错误信息："+e);
        }
    }


}
