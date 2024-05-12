package cn.mw.monitor.service.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 数字孪生监控信息DTO
 * @date 2023/8/3 10:03
 */
@Data
@ApiModel("数字孪生监控信息DTO")
public class MwDigitalTwinItemDto {

    @ApiModelProperty("CPU使用率")
    private String cpuUtilization;

    @ApiModelProperty("CPU温度")
    private String cpuTemperature;

    @ApiModelProperty("内存总容量")
    private String memoryTotal;

    @ApiModelProperty("内存使用率")
    private String memoryUtilization;

    @ApiModelProperty("内存剩余使用率")
    private String memoyFreeUtilization;

    @ApiModelProperty("磁盘总容量")
    private String diskTotal;

    @ApiModelProperty("磁盘使用率")
    private String diskUtilization;

    @ApiModelProperty("磁盘剩余使用率")
    private String diskFreeUtilization;

    @ApiModelProperty("磁盘已使用")
    private String diskUsed;

    @ApiModelProperty("接口数量")
    private Integer interfaceConut;

    @ApiModelProperty("接口信息")
    private List<MwDigitalTwinInterfaceDto> interfaceDtos;

    @ApiModelProperty("告警信息DTO")
    private MwDigitalTwinAlertDto alertDto;
}
