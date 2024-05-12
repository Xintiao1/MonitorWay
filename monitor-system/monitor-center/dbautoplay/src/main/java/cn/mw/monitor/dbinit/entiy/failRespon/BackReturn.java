package cn.mw.monitor.dbinit.entiy.failRespon;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BackReturn {
    String sql;
    Boolean back;
    String failreson;
}
