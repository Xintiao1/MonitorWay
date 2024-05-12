package cn.mw.monitor.service.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author gengjb
 * @description 数字孪生告警DTO
 * @date 2023/8/17 10:59
 */
@Data
@ApiModel("数字孪生告警DTO")
public class MwDigitalTwinAlertDto {

    @ApiModelProperty("告警总数")
    private Integer alertCount;

    @ApiModelProperty("最后时间")
    private String lastTime;

    @ApiModelProperty("告警信息")
    private List alertInfos;

    @ApiModelProperty("告警分类")
    private Map<String,Integer> alertClassift;

    @ApiModelProperty("公网出口数")
    private Integer linkCount;

    @ApiModelProperty("公网总带宽")
    private Double totalBandWidth;

    @ApiModelProperty("带宽单位")
    private String bandWidthUnit;
    @ApiModelProperty("告警设备总数")
    private int alertDeviceCount;
    @ApiModelProperty("告警设备类型总数")
    private int alertDeviceTypeCount;
}
