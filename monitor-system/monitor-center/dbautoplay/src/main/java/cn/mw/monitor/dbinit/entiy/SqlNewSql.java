package cn.mw.monitor.dbinit.entiy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;


//其他sql
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SqlNewSql {
    String sqlString;
    Date createTime;
    String cteator;
    //唯一标志帮忙确定sql
    Integer id;
}
