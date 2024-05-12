package cn.mw.monitor.activiti.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/10/14 11:14
 * @Version 1.0
 */
@Data
public class BatchPassParam {
    /**
     * 任务Id
     */
    private String taskId;
    /**
     * 流程实例id
     */
    private String processId;
    /**
     * 知识id
     */
    private String KnowledgeId;
}
