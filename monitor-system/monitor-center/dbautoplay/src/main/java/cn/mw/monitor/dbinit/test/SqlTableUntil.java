package cn.mw.monitor.dbinit.test;

import cn.mw.monitor.dbinit.entiy.*;
import cn.mw.monitor.dbinit.entiy.failRespon.BackReturn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlTableUntil {

   public static String TABLE_SCHEMA = "monitor";


    public static List<SqlTable> getSqlTable(Statement stmt) throws SQLException {
        String sql;
        sql = "SELECT TABLE_NAME,TABLE_TYPE,CREATE_TIME,UPDATE_TIME,CHECK_TIME,TABLE_COMMENT,TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA = '"+TABLE_SCHEMA+"'";
        ResultSet rs = stmt.executeQuery(sql);
        List<SqlTable> sqlTables = new ArrayList<>();
        // 展开结果集数据库
        while(rs.next()){
            // 通过字段检索
            SqlTable sqlTable = new SqlTable();
            sqlTable.setTABLE_NAME(rs.getString("TABLE_NAME")).setTABLE_TYPE(rs.getString("TABLE_TYPE"))
                    .setCHECK_TIME(rs.getDate("CHECK_TIME")).setCREATE_TIME(rs.getDate("CREATE_TIME")).setUPDATE_TIME(rs.getDate("UPDATE_TIME"))
                    .setTABLE_COLLATION(rs.getString("TABLE_COLLATION")).setTABLE_COMMENT(rs.getString("TABLE_COMMENT"));
            sqlTables.add(sqlTable);
        }

        // 完成后关闭
        rs.close();
        for (SqlTable sqlTable:sqlTables) {
            sqlTable.setCreate_Sql(getCreateSql(stmt,sqlTable.getTABLE_NAME(),sqlTable.getTABLE_TYPE()));
            sqlTable.setSqlTable_keys(getSqlTableKey(stmt,sqlTable.getTABLE_NAME()));
            sqlTable.setSqlTable_indexes(getSqlTableIndex(stmt,sqlTable.getTABLE_NAME()));
            sqlTable.setSqlTable_foreign_keys(getSqlTableForeignKeys(stmt,sqlTable.getTABLE_NAME()));
        }
        return sqlTables;
    }


    public static List<SqlNewSql> getNewSqls(Statement stmt) throws SQLException {
        String sql;
        sql = "SELECT * FROM mw_db_auto";
        ResultSet rs = stmt.executeQuery(sql);
        List<SqlNewSql> sqlNewSqls = new ArrayList<>();
        // 展开结果集数据库
        while(rs.next()){
            // 通过字段检索
            SqlNewSql sqlNewSql = new SqlNewSql();
            sqlNewSql.setCreateTime(rs.getDate("create_time"))
                    .setCteator(rs.getString("creator")).setSqlString(rs.getString("sql_string"))
                   .setId(rs.getInt("id"));
            sqlNewSqls.add(sqlNewSql);
        }

        // 完成后关闭
        rs.close();

        return sqlNewSqls;
    }


    private static List<SqlTable_Foreign_Key> getSqlTableForeignKeys(Statement stmt, String table_name) throws SQLException {
        String sql;
        sql = "select * from information_schema.KEY_COLUMN_USAGE WHERE  TABLE_NAME = ' "+table_name+"'";
        ResultSet rs = stmt.executeQuery(sql);
        List<SqlTable_Foreign_Key> sqlTable_foreign_keys = new ArrayList<>();
        // 展开结果集数据库
        while(rs.next()){
            // 通过字段检索
            SqlTable_Foreign_Key sqlTable_foreign_key = new SqlTable_Foreign_Key();
            sqlTable_foreign_key.setCONSTRAINT_NAME(rs.getString("CONSTRAINT_NAME")).setCOLUMN_NAME(rs.getString("COLUMN_NAME"))
                    .setORDINAL_POSITION(rs.getInt("ORDINAL_POSITION")).setPOSITION_IN_UNIQUE_CONSTRAINT(rs.getInt("POSITION_IN_UNIQUE_CONSTRAINT"))
                    .setREFERENCED_TABLE_NAME(rs.getString("REFERENCED_TABLE_NAME")).setREFERENCED_COLUMN_NAME(rs.getString("REFERENCED_COLUMN_NAME"));
            sqlTable_foreign_keys.add(sqlTable_foreign_key);
        }

        // 完成后关闭
        rs.close();

        return sqlTable_foreign_keys;
    }

    private static List<SqlTable_Indexes> getSqlTableIndex(Statement stmt, String table_name) throws SQLException {

        String sql;
        sql = "SHOW INDEX FROM  "+table_name;
        ResultSet rs = stmt.executeQuery(sql);
        List<SqlTable_Indexes> sqlTable_indexes = new ArrayList<>();
        // 展开结果集数据库
        while(rs.next()){
            // 通过字段检索
            SqlTable_Indexes sqlTable_index = new SqlTable_Indexes();
            sqlTable_index.setIndex_type(rs.getString("Index_type")).setSeq_in_index(rs.getInt("Seq_in_index"))
                    .setColumn_name(rs.getString("Column_name")).setKey_name(rs.getString("Key_name"))
            .setNon_unique(rs.getInt("Non_unique")).setComment(rs.getString("Comment"));
            sqlTable_indexes.add(sqlTable_index);
        }

        // 完成后关闭
        rs.close();


        return sqlTable_indexes;
    }

    public static String getCreateSql(Statement stmt, String table_name,String type) throws SQLException {
        String sql;

            sql = "SHOW CREATE TABLE "+table_name;


        ResultSet rs = stmt.executeQuery(sql);
        String createSql ="";
        // 展开结果集数据库
        while(rs.next()){
            // 通过字段检索
            try{
                if (type.equals("BASE TABLE")) {
                    createSql = rs.getString("Create Table");
                }
                else {
                    createSql = rs.getString("Create View");
                }
            }catch (Exception e){
                /*//System.out.println(table_name);*/
            }
        }
        // 完成后关闭
        rs.close();
        return createSql;
    }
    
    
    

    private static List<SqlTable_Key> getSqlTableKey(Statement stmt, String table_name) throws SQLException {
        String sql;
        sql = "SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '"+TABLE_SCHEMA +"' AND TABLE_NAME = '"+table_name+"'";
        ResultSet rs = stmt.executeQuery(sql);
        List<SqlTable_Key> sqlTable_keys = new ArrayList<>();
        // 展开结果集数据库
        while(rs.next()){
            // 通过字段检索
            SqlTable_Key sqlTable_key = new SqlTable_Key();
            sqlTable_key.setCOLUMN_NAME(rs.getString("COLUMN_NAME")).setORDINAL_POSITION(rs.getInt("ORDINAL_POSITION"))
                    .setCOLUMN_DEFAULT(rs.getString("COLUMN_DEFAULT")).setIS_NULLABLE(rs.getString("IS_NULLABLE"))
                    .setDATA_TYPE(rs.getString("DATA_TYPE")).setCOLUMN_TYPE(rs.getString("COLUMN_TYPE")).setCOLUMN_KEY(rs.getString("COLUMN_KEY"))
                    .setEXTRA(rs.getString("EXTRA"));
            sqlTable_keys.add(sqlTable_key);
        }

        // 完成后关闭
        rs.close();
        return sqlTable_keys;
    }

    public static List<BackReturn> UpdateSql(Statement stmt, List<String> sql) throws SQLException {
        List<BackReturn> backReturns = new ArrayList<>();

        for (String s:sql) {
            BackReturn backReturn = new BackReturn();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            s = s;
            backReturn.setSql(s);
            try{
                stmt.execute(s);
                backReturn.setBack(true);
            }catch (Exception e){

                backReturn.setBack(false);
                backReturn.setFailreson(e.toString());
            }
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            backReturns.add(backReturn);
        }
        return backReturns;
    }
}
