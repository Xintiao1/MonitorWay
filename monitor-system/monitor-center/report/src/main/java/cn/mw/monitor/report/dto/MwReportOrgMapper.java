package cn.mw.monitor.report.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/15 14:06
 */
@Data
@Builder
public class MwReportOrgMapper {
    private Integer reportId;

    private Integer orgId;
}
