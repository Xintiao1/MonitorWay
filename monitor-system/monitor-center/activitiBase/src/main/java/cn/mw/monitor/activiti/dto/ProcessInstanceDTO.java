package cn.mw.monitor.activiti.dto;

import java.util.HashMap;

public class ProcessInstanceDTO {
    private String assignee;
    private String processDefinitionKey;
    private String processInstanceName;
    private String businessKey;
    private HashMap variables;

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessInstanceName() {
        return processInstanceName;
    }

    public void setProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public HashMap getVariables() {
        return variables;
    }

    public void setVariables(HashMap variables) {
        this.variables = variables;
    }
}
