package cn.mw.monitor.report.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/11 11:17
 */
@Data
@Builder
public class MwTimeActionMapper {
    private String reportId;
    private Integer actionId;

}
