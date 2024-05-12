package cn.mw.monitor.dbinit.entiy;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/*最终表数据*/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SqlTable {
    //数据表的名称
    String TABLE_NAME;
    //数据表种类：BASE TABLE;VIEW
    String TABLE_TYPE;
    //表创建时间
    Date CREATE_TIME;
    //表修改时间
    Date UPDATE_TIME;
    //表检查时间
    Date CHECK_TIME;
    //字符集
    String TABLE_COLLATION;
    //描述
    String TABLE_COMMENT;
    //如果表不存在新增数据
    String Create_Sql;
    //属性字段
    List<SqlTable_Key> sqlTable_keys;
    //外键
    List<SqlTable_Foreign_Key> sqlTable_foreign_keys;
    //索引
    List<SqlTable_Indexes> sqlTable_indexes;
}
