package cn.mw.monitor.report.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/11 10:26
 */
@Data
@Builder
public class MwReportGroupMapper {
    private Integer reportId;

    private Integer groupId;
}
