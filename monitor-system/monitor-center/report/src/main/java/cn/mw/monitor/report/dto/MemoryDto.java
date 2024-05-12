package cn.mw.monitor.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/5/12 10:19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemoryDto {
    private String hostId;
    private String ipAddress;
    private String assetsName;
    private Double memoryMaxValue;
    private Double memoryMinValue;
    private Double memoryAvgValue;
}
