package cn.mw.monitor.activiti.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/20 11:39
 * @Version 1.0
 */
@Data
public class SelectFlowViewParams {
    //流程实例id
    private String processId;
    //流程定义id
    private String deploymentId;
}
