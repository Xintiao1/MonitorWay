package cn.mw.monitor.dbinit.config;

import cn.mw.monitor.dbinit.entiy.SqlNewSql;
import cn.mw.monitor.dbinit.entiy.SqlTable;
import cn.mw.monitor.dbinit.entiy.failRespon.BackReturn;
import cn.mw.monitor.dbinit.test.SqlContrast;
import cn.mw.monitor.dbinit.test.SqlTableUntil;
import cn.mw.monitor.util.RSAUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;

@Slf4j
@Component
public class DBInitComponent {
    @Value("${spring.datasource.url}")
    private String DB_URL;

    @Value("${spring.datasource.username}")
    private String USER;

    @Value("${spring.datasource.password}")
    private String PASS;

    @Value("${spring.datasource.driverClassName}")
    private String JDBC_DRIVER;

    @Value("${dbautoplay.readOrWrite}")
    private boolean readOrWrite;

    @Value("${dbautoplay.enable}")
    private boolean enable;

    /**
     * 是否加密
     */
    @Value("${spring.datasource.isencrypt}")
    private boolean isEncrypt;

    static final String path = "templates/dbSqlUpdate.txt";

    static final String writepath = "dbSqlUpdate.txt";
    static final String writeInsert = "numInsertOrUpdate.txt";
    // 初始化任务
    @Bean
    public void DBInitComponentTime() {
        if(!enable){
            return;
        }
        log.info("连接数据库...");
        /*        //System.out.println("");*/
        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
            /*json.string*/
            // 打开链接
            log.info("连接数据库...");
            String password = PASS;
            if (isEncrypt) {
                //password解密
                password = RSAUtils.decryptData(PASS, RSAUtils.RSA_PRIVATE_KEY);
            }
            conn = DriverManager.getConnection(DB_URL, USER, password);

            // 执行查询
            log.info(" 实例化Statement对象...");
            stmt = conn.createStatement();

            List<SqlTable> sqlTables = SqlTableUntil.getSqlTable(stmt);
            List<SqlNewSql> sqlNewSqls = SqlTableUntil.getNewSqls(stmt);
            if (!readOrWrite) {
                String s = JSON.toJSONString(sqlTables);
                clearInfoForFile(writepath);
                writeText(writepath, s);

                String m = JSON.toJSONString(sqlNewSqls);
                writeText(writeInsert, m);
                /*String newS = txt2String(path);
                List<SqlTable> newSql = JSON.parseArray(newS, SqlTable.class);
                List<String> sql = SqlContrast.Contrast(sqlTables, newSql);
                log.info(sql.toString());*/

            }
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            log.error("错误返回 :{}",se);
        } catch (Exception e) {
            // 处理 Class.forName 错误
            log.error("错误返回 :{}",e);
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                log.error("错误返回 :{}",se);
            }
        }
        /* //System.out.println("Goodbye!");*/
    }


    public static void writeText(String path, String text) throws IOException {
        String kill = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        String bill[] = kill.split("monitor-system");
        String c = bill[0];
        path  = bluidTest(c,"/monitor-system/monitor-center/dbautoplay/src/main/resources/templates/",path);

       /* c = c.replaceAll("/", "\\");
        path = c + "\\monitor-system\\monitor-center\\dbautoplay\\src\\main\\resources\\templates\\" + path;*/
        /*  path  = bluidTest(c,"\\monitor-system\\monitor-center\\dbautoplay\\src\\main\\resources\\templates\\",path);*/
        File file = new File(path);
        try {
            FileOutputStream o = new FileOutputStream(file);

            o.write(text.getBytes("UTF-8"));

            o.close();

        } catch (Exception e) {

        }

    }

    private static String bluidTest(String c, String s, String path) {
        String osName = System.getProperty("os.name");
        s = s.replaceAll("/", Matcher.quoteReplacement(File.separator));
        if (osName.startsWith("Mac OS")) {
            path = c + s + path;
            // 苹果
        } else if (osName.startsWith("Windows")) {
            // windows
        /*    c = c.replaceFirst("/", "");*/
            path = c + s+ path;
        } else {
            // unix or linux
            path = c + s+ path;
        }

        return path;
    }


    public static void clearInfoForFile(String path) throws IOException {
        String kill = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        String bill[] = kill.split("monitor-system");
        String c = bill[0];
        path  = bluidTest(c,"/monitor-system/monitor-center/dbautoplay/src/main/resources/templates/",path);
       /* String c = bill[0].replaceFirst("/", "");
        c = c.replaceAll("/", "\\");
        path = c + "\\monitor-system\\monitor-center\\dbautoplay\\src\\main\\resources\\templates\\" + path;*/
        /*    path  = bluidTest(c,"\\monitor-system\\monitor-center\\dbautoplay\\src\\main\\resources\\templates\\",path);*/
        File file = new File(path);
        FileWriter fileWriter = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            try {
                fileWriter.flush();
                fileWriter.close();
                log.error("错误返回 :{}",e);
            }catch (Exception b){
                log.error("正常结束 :{}");
            }

        }
    }


}
