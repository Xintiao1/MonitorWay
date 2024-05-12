package cn.mw.monitor.dbinit.entiy;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

//数据表的索引
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SqlTable_Foreign_Key {
    //外键名称
    String CONSTRAINT_NAME;
    //外键绑定键值
    String COLUMN_NAME;
    //定序位置
    Integer ORDINAL_POSITION;
    //NULL对于唯一和主键约束，对于外键约束，此列是正在引用的表的键中的序号位置
    Integer POSITION_IN_UNIQUE_CONSTRAINT;
    //具有约束的表的名称
    String REFERENCED_TABLE_NAME;
    //具有约束的表的对应外键
    String REFERENCED_COLUMN_NAME;
}
