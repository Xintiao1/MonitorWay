package cn.mw.monitor.service.virtual.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author qzg
 * @date 2022/8/25
 */
@Data
public class PerformanceManage {
    private Date startTime;
    private Date endTime;
    private List<PerformanceInfo> performanceValues;
    private String unit;
}
