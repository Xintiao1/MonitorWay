package cn.mw.monitor.assets.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/6/12 11:14
 */
@Data
public class AssetsIotDto {
    private String assetsId;
    @ApiModelProperty("温度告警阈值")
    private Double temThreshold;
    @ApiModelProperty("温度告警条件<>=")
    private String temCondition;
    @ApiModelProperty("告警声音是否开启，ON开启 OFF关闭")
    private Boolean voice;
    @ApiModelProperty("湿度告警阈值")
    private Double humThreshold;
    @ApiModelProperty("湿度告警条件<>=")
    private String humCondition;
}
