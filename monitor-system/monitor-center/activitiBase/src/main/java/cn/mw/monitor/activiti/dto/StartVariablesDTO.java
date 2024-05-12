package cn.mw.monitor.activiti.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author syt
 * @Date 2020/10/9 17:05
 * @Version 1.0
 */
@Data
public class StartVariablesDTO {
    //流程图的id
    private String instanceKey;
    //流程变量
    private Map<String, Object> variables;
}
