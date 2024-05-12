package cn.mw.monitor.activiti.dto;

import lombok.Data;

@Data
public class ProcessModuleBindDTO {
    private int id;
    private String activitiProcessId;
    private String modelName;
    private String action;
}
