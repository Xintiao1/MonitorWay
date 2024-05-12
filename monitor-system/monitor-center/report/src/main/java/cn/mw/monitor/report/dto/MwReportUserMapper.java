package cn.mw.monitor.report.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/11 10:25
 */
@Data
@Builder
public class MwReportUserMapper {
    private Integer reportId;

    private Integer userId;
}
