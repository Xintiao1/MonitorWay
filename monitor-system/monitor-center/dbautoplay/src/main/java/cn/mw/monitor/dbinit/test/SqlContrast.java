package cn.mw.monitor.dbinit.test;

import cn.mw.monitor.dbinit.entiy.SqlNewSql;
import cn.mw.monitor.dbinit.entiy.SqlTable;
import cn.mw.monitor.dbinit.entiy.SqlTable_Key;

import java.util.ArrayList;
import java.util.List;

public class SqlContrast {


    public static List<String> Contrast(List<SqlTable> sqlTables, List<SqlTable> newSql) {
        List<String> sql = new ArrayList<>();
        for (SqlTable newsql:newSql) {
            Boolean haveTable = false;
            for (SqlTable oldsql:sqlTables) {
                if (oldsql.getTABLE_NAME().equals(newsql.getTABLE_NAME())){
                    haveTable = true;
                    List<String> strings = ContrastTable(oldsql,newsql);
                    if (strings.size()>0){
                        sql.addAll(strings);
                    }
                }
            }
            if (!haveTable){
                sql.add(newsql.getCreate_Sql());
            }

        }
        return sql;
    }


    public static List<SqlNewSql> ContrastInsert(List<SqlNewSql> sqlTables, List<SqlNewSql> newSql) {
        List<SqlNewSql> sql = new ArrayList<>();
        for (SqlNewSql newsql:newSql) {
            Boolean haveTable = false;
            for (SqlNewSql oldsql:sqlTables) {
                if (oldsql.getId().equals(newsql.getId())){
                    haveTable = true;
                }
            }
            if (!haveTable){
                sql.add(newsql);
            }
       /*   if (!sqlTables.contains(newsql)){
              sql.add(newsql);
          }*/
        }
        return sql;
    }

    private static List<String> ContrastTable(SqlTable oldsql, SqlTable newsql) {
        //主键对比
        List<String> sql = ContrastSqlKey(oldsql,newsql);


        //外键对比

        //索引对比

        return sql;
    }

    private static List<String> ContrastSqlKey(SqlTable oldsql, SqlTable newsql) {
        if (oldsql.getTABLE_NAME().equals("mw_db_version")){
            /*//System.out.println(1111);*/
        }
        List<String> sql = new ArrayList<>();

        for (SqlTable_Key newsqlTable_key:newsql.getSqlTable_keys()) {
            Boolean haveTable = false;
            for (SqlTable_Key oldSqlTable_keys:oldsql.getSqlTable_keys()) {
                String basic = "ALTER TABLE `"+SqlTableUntil.TABLE_SCHEMA+"`.`"+newsql.getTABLE_NAME()+"`";
                    if (oldSqlTable_keys.getCOLUMN_NAME().equals(newsqlTable_key.getCOLUMN_NAME())){

                        haveTable = true;
                        //修改对比
                        basic =basic+" MODIFY COLUMN  `"+newsqlTable_key.getCOLUMN_NAME()+"`";
                        Boolean change = false;
                        if (!oldSqlTable_keys.getCOLUMN_TYPE().equals(newsqlTable_key.getCOLUMN_TYPE())){
                            change = true;
                        }
                        basic = basic+newsqlTable_key.getCOLUMN_TYPE();
                        if (!oldSqlTable_keys.getIS_NULLABLE().equals(newsqlTable_key.getIS_NULLABLE())){
                            change = true;
                            if (newsqlTable_key.getIS_NULLABLE().equals("YES")){
                                basic = basic+" NULL";
                            }else {
                                basic = basic+" NOT NULL";
                            }
                        }

                        if (newsqlTable_key.getIS_NULLABLE().equals("YES")){
                            basic = basic+" NULL";
                        }else {
                            basic = basic+" NOT NULL";
                        }

                        if (concatNull(newsqlTable_key.getCOLUMN_DEFAULT(),oldSqlTable_keys.getCOLUMN_DEFAULT())) {
                            change = true;
                            if (newsqlTable_key.getCOLUMN_DEFAULT()==null) {

                            } else {
                                basic = basic + " DEFAULT " + newsqlTable_key.getCOLUMN_DEFAULT();
                            }
                        }

                        if (newsqlTable_key.getCOLUMN_DEFAULT()==null) {

                        } else {
                            basic = basic + " DEFAULT " + newsqlTable_key.getCOLUMN_DEFAULT();
                        }

                        if (newsqlTable_key.getEXTRA().equals("auto_increment")){
                            basic = basic+" AUTO_INCREMENT";
                        }
                        if (change){
                            sql.add(basic);
                        }
                    }
            }
            if (!haveTable){
                String basic = "ALTER TABLE `"+SqlTableUntil.TABLE_SCHEMA+"`.`"+newsql.getTABLE_NAME()+"`";
                basic =basic+" ADD ";
                List<String> strings = CreateTableColumn(basic,newsqlTable_key,newsql.getCreate_Sql());
                sql.addAll(strings);
            }
        }


        return sql;
    }

    private static boolean concatNull(String column_default, String column_default1) {
        if (column_default==null&&column_default1!=null){
            return true;
        }
        if (column_default!=null&&column_default1==null){
            return true;
        }
        if (column_default==null&&column_default1==null){
            return false;
        }
        if (column_default.equals(column_default1)){
            return false;
        }
        else {
            return true;
        }
    }

    private static List<String> CreateTableColumn(String basic, SqlTable_Key newsqlTable_key,String Createsql) {
        String [] strings = Createsql.split("\n");
        List<String> sql =  new ArrayList<>();
        for (String s:strings) {
            if (s.contains("`"+newsqlTable_key.getCOLUMN_NAME()+"`")){
                String t = s.replace(",","");
                if (t.contains("INDEX")||t.contains("CONSTRAINT")||t.contains("PRIMARY")){
                    String sq = basic+t;
                    sql.add(sq);
                }else {
                    String sq = basic+" COLUMN"+t;
                    sql.add(sq);
                }
            }
        }
        return sql;

    }


    public static void main(String[] args) {
        String createsql = "CREATE TABLE `ACT_GE_BYTEARRAY` (\n" +
                "  `ID_` varchar(64) COLLATE utf8_bin NOT NULL,\n" +
                "  `REV_` int(11) DEFAULT NULL,\n" +
                "  `NAME_` varchar(255) COLLATE utf8_bin DEFAULT NULL,\n" +
                "  `DEPLOYMENT_ID_` varchar(64) COLLATE utf8_bin DEFAULT NULL,\n" +
                "  `BYTES_` longblob DEFAULT NULL,\n" +
                "  `GENERATED_` tinyint(4) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`ID_`) USING BTREE,\n" +
                "  KEY `ACT_FK_BYTEARR_DEPL` (`DEPLOYMENT_ID_`) USING BTREE,\n" +
                "  CONSTRAINT `ACT_FK_BYTEARR_DEPL` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `ACT_RE_DEPLOYMENT` (`ID_`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC";
        String [] strings = createsql.split("\n");
        for (String s:strings) {
            if (s.contains("`REV_`")){
                String t = s.replace(",","");
               /* //System.out.println(t);*/
            }
        }
    }
}
