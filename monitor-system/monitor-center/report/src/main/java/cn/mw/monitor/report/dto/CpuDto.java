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
public class CpuDto {
    private String hostId;
    private Double cpuMaxValue;
    private Double cpuMinValue;
    private Double cpuAvgValue;
}
