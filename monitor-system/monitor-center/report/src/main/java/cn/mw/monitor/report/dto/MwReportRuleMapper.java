package cn.mw.monitor.report.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/11/17 15:51
 */
@Data
@Builder
public class MwReportRuleMapper {
    private String reportId;
    private String ruleId;
    private Integer ruleType;
}
