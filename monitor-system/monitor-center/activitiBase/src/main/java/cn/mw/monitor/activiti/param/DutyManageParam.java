package cn.mw.monitor.activiti.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;
import java.util.Date;
@Data
public class DutyManageParam extends BaseParam {

    private String userName;
    private Integer userId;
    private Integer orgId;
    private String orgName;
    private Integer groupId;
    private String groupName;
    private Date date;
    private String shiftId;
    private String shiftName;
    //1：本月；2：今日；3：自定义；
    private Integer type;
    private String startDate;
    private String endDate;
}
