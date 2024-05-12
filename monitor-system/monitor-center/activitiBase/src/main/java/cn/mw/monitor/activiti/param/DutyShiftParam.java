package cn.mw.monitor.activiti.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DutyShiftParam extends BaseParam {

    private String id;
    private String shiftName;
    private String dutyStartDate;
    private String dutyEndDate;
    private Boolean isTwoDay;
    private Date createDate;
    private List<String> ids;
}
