package cn.mw.monitor.dbinit.entiy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lumingming
 * @createTime 07 19:06
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SqlTable_Key {
    //字段名称
    String COLUMN_NAME;
    //字段排序位置
    Integer ORDINAL_POSITION;
    //默认值
    String COLUMN_DEFAULT ;
    //是否为空
    String IS_NULLABLE;
    //字段类型
    String DATA_TYPE;
    //字段详情类型
    String COLUMN_TYPE;
    //字段存在类型主键，外键
    String COLUMN_KEY;
    //字段是否自增 改变函数
    String EXTRA;
}
