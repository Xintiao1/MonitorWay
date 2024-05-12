package cn.mw.monitor.activiti.entiy;

import lombok.Data;
import java.util.Date;

@Data
public class DutyEntity {
    private String id;
    private Integer userId;
    private Date date;
    private Integer createUser;
    private String shiftId;
}
