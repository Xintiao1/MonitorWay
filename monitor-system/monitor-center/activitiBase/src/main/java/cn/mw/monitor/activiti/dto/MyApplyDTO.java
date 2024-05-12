package cn.mw.monitor.activiti.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2020/9/21 16:02
 * @Version 1.0
 */
@Data
public class MyApplyDTO {
    private String id;
    private String name;
    private Date startTime;
    private String status;

    public MyApplyDTO(String id, String name, Date startTime) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
    }

    public MyApplyDTO() {
    }
}
