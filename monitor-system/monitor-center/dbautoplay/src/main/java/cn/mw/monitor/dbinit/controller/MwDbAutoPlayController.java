package cn.mw.monitor.dbinit.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.dbinit.entiy.SqlNewSql;
import cn.mw.monitor.dbinit.entiy.SqlTable;
import cn.mw.monitor.dbinit.entiy.failRespon.BackReturn;
import cn.mw.monitor.dbinit.test.SqlContrast;
import cn.mw.monitor.dbinit.test.SqlTableUntil;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author syt
 * @Date 2021/9/29 15:26
 * @Version 1.0
 */
@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "测试auto模块是否导入", tags = "")
public class MwDbAutoPlayController extends BaseApiService {

    @Value("${spring.datasource.url}")
    private String DB_URL;

    @Value("${spring.datasource.username}")
    private String USER;

    @Value("${spring.datasource.password}")
    private String PASS;

    @Value("${spring.datasource.driverClassName}")
    private String JDBC_DRIVER;

    /**
     * 是否加密
     */
    @Value("${spring.datasource.isencrypt}")
    private boolean isEncrypt;


    static final String path = "templates/dbSqlUpdate.txt";

    static final String writeInsert = "templates/numInsertOrUpdate.txt";


    @PostMapping("/auto/test")
    @ResponseBody
    @ApiOperation(value = "测试auto模块是否导入")
    public ResponseBase wordOut() {
        log.info("连接数据库...");
        /*        //System.out.println("");*/
        Connection conn = null;
        Statement stmt = null;
        List<BackReturn> backReturn = new ArrayList<>();
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

            String newS = txt2String(path);
            log.info(newS);
            List<SqlTable> newSql = JSON.parseArray(newS, SqlTable.class);
            List<String> sql = SqlContrast.Contrast(sqlTables, newSql);
            backReturn = SqlTableUntil.UpdateSql(stmt, sql);
            log.info("*********数据库新增**********");
            log.info(sql.toString());

            //自动执行数据库新增语句

            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
           log.error("错误信息："+se);
        } catch (Exception e) {
            // 处理 Class.forName 错误
            log.error("错误信息："+e);
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                log.error("错误信息："+se);
            }
        }

        try {
            insertDatabase();
        }catch (Exception e){
            log.error("初始化relasesql："+e);
        }
        return setResultSuccess(Reply.ok(backReturn));
    }

    @PostMapping("/auto/database")
    @ResponseBody
    @ApiOperation(value = "测试auto模块是否导入")
    public ResponseBase writeDatabase() {
        log.info("连接数据库...");
        /*        //System.out.println("");*/
        Connection conn = null;
        Statement stmt = null;
        List<BackReturn> backReturn = new ArrayList<>();
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

            String newS = txt2String(path);
            log.info(newS);
            List<SqlTable> newSql = JSON.parseArray(newS, SqlTable.class);
            List<String> sql = SqlContrast.Contrast(sqlTables, newSql);
            backReturn = SqlTableUntil.UpdateSql(stmt, sql);
            log.info("*********数据库新增**********");
            log.info(sql.toString());

            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            log.error("错误信息："+se);
        } catch (Exception e) {
            // 处理 Class.forName 错误
            log.error("错误信息："+e);
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
                log.error("错误信息："+se2);
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                log.error("错误信息："+se);
            }
        }
        try {
            insertDatabase();
        }catch (Exception e){
            log.error("初始化relasesql："+e);
        }

        return setResultSuccess(Reply.ok(backReturn));
    }

    @PostMapping("/auto/insertDatabase")
    @ResponseBody
    @ApiOperation(value = "插入需要运行的新语句")
    public ResponseBase insertDatabase() {
        log.info("连接数据库...");
        /*        //System.out.println("");*/
        Connection conn = null;
        Statement stmt = null;
        List<BackReturn> backReturn = new ArrayList<>();
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


            List<SqlNewSql> sqlNewSqls = SqlTableUntil.getNewSqls(stmt);
            String newS = txt2String(writeInsert);
            log.info(newS);
            List<SqlNewSql> newSql = JSON.parseArray(newS, SqlNewSql.class);

            List<SqlNewSql> sql = SqlContrast.ContrastInsert(sqlNewSqls, newSql);
            List<String> strings = new ArrayList<>();
            for (SqlNewSql s:sql) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String d = format.format(s.getCreateTime());
                String l = "INSERT INTO `mw_db_auto`(`id`, `creator`, `create_time`, `sql_string`) VALUES ";
                 l = l+"("+s.getId()+", '"+s.getCteator()+"', '"+d+"', \""+s.getSqlString()+"\");";
                 strings.add(l);
                 strings.addAll(Arrays.asList(s.getSqlString().split(";")));
            }
            backReturn = SqlTableUntil.UpdateSql(stmt, strings);
            log.info("*********数据库新增**********");
            log.info(sql.toString());

            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            log.error("错误信息："+se);
        } catch (Exception e) {
            // 处理 Class.forName 错误
            log.error("错误信息："+e);
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
                log.error("错误信息："+se2);
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                log.error("错误信息："+se);
            }
        }
        return setResultSuccess(Reply.ok(backReturn));
    }



    public String txt2String(String path) throws FileNotFoundException {
        /*  File file = new File(path);*//*文件名*/
        /* File file = ResourceUtils.getFile("classpath:"+path);*/
        InputStream inputStream = this.getClass().getResourceAsStream("/" + path);
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            ;//构造一个BufferedReader类来读取文件
            String s = null;
            while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                result.append(System.lineSeparator() + s);
            }
            br.close();
        } catch (Exception e) {
            log.error("错误信息："+e);

        }
        return result.toString();
    }
}