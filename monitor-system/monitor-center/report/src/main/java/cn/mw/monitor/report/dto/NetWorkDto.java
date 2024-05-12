package cn.mw.monitor.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author xhy
 * @date 2020/5/15 15:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetWorkDto extends BaseDto {

    private String netName;

    private double netInBpsMaxValue;
    private double netInBpsMinValue;
    private double netInBpsAvgValue;

    private double netOutBpsMaxValue;
    private double netOutBpsMinValue;
    private double netOutBpsAvgValue;
}
