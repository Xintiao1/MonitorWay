package cn.mw.monitor.activiti.dto;

/**
 * @author syt
 * @Date 2020/10/26 16:49
 * @Version 1.0
 */
public enum ProcessVariablesNameEnum {
    USER("user"),
    APPROVE("approve"),
    AUDIT("audit"),
    KNOWLEDGE_BASE_PARAM("knowledgeBaseParam")
    ;

    ProcessVariablesNameEnum(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ProcessVariablesNameEnum() {
    }
}
