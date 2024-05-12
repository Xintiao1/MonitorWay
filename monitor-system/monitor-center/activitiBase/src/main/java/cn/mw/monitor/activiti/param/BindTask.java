package cn.mw.monitor.activiti.param;

import lombok.Data;

/**
 * @author lumingming
 * @createTime 2023814 21:50
 * @description
 */
@Data
public class BindTask {
    private int id;
    private String activitiProcessId;
    private String processInstanceKey;
    private String modelId;
    private String taskId;
    private Integer isStart=0;
    private String position;
}
