package cn.mw.monitor.api.controller.dto;

import lombok.Data;

@Data
public class MonitorDTO {
    private Integer ruleId;
    private String threadName;
    private int allCount;
    private int errorCount;
    private int successCount;
}
