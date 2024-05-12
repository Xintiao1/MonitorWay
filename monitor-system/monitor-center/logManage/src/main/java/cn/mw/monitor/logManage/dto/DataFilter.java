package cn.mw.monitor.logManage.dto;

import lombok.Data;

@Data
public class DataFilter {

    /**
     * 过滤字段
     */
    private String srcField;

    /**
     * 过滤动作
     */
    private String action;

    /**
     * 过滤规则
     */
    private String ruleContent;
}
