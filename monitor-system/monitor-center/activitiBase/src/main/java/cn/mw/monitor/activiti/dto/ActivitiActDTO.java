package cn.mw.monitor.activiti.dto;

import cn.mw.monitor.service.knowledgeBase.model.MwKnowledgeBaseTable;
import lombok.Data;
import org.activiti.engine.task.DelegationState;

import java.util.Date;

/**
 * @author syt
 * @Date 2020/9/21 14:35
 * @Version 1.0
 */
@Data
public class ActivitiActDTO extends MwKnowledgeBaseTable {
    private String taskId;
    private String name;
    private DelegationState status;
    private String executionId;
    private Date createTime;

    public ActivitiActDTO(String taskId, String name, DelegationState status, String executionId, Date createTime) {
        this.taskId = taskId;
        this.name = name;
        this.status = status;
        this.executionId = executionId;
        this.createTime = createTime;
    }

    public ActivitiActDTO() {
    }
}
