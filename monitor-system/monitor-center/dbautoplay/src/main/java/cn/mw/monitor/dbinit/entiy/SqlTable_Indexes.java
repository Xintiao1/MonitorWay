package cn.mw.monitor.dbinit.entiy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


//数据表的索引
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SqlTable_Indexes {
    //是否唯一
    Integer Non_unique;
    //字段排序位置
    Integer Seq_in_index;
    //外键名称
    String Key_name;
    //关联字段
    String Column_name;
    //字段类型
    String Index_type;
    //注释
    String Comment;
}
