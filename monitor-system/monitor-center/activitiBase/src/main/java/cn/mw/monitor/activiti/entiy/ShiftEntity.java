package cn.mw.monitor.activiti.entiy;

import lombok.Data;

@Data
public class ShiftEntity {
    private String id;
    private String shiftName;
    private String dutyStartDate;
    private String dutyEndDate;
    private Boolean isTwoDay;
    private Integer createUser;

    private Integer updateUser;
}
