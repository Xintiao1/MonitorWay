package cn.mw.monitor.service.activiti.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;

@Data
public class DutyCommonsParam {

    //1：本月；2：今日；3：自定义；
    private Integer type;
    private String startDate;
    private String endDate;
}
