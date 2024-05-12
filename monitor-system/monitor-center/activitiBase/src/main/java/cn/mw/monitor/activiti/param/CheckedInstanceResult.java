package cn.mw.monitor.activiti.param;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class CheckedInstanceResult {
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date lastCheckedDate;
    private int nodeStatus;
}
