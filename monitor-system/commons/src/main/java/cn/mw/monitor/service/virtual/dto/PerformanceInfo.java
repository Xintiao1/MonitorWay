package cn.mw.monitor.service.virtual.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author qzg
 * @date 2022/9/21
 */
@Data
public class PerformanceInfo {
    private Long value;
    private Date time;
}
