package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 接口信息DTO
 * @Author gengjb
 * @Date 2023/5/19 10:47
 * @Version 1.0
 **/
@Data
@ApiModel("接口信息DTO")
public class MwVisualioduleInterfaceDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("接口名称")
    private String interfaceName;

    @ApiModelProperty("接收值")
    private String acceptValue;

    @ApiModelProperty("接收单位")
    private String acceptUnits;

    @ApiModelProperty("发送值")
    private String sendValue;

    @ApiModelProperty("发送单位")
    private String sendUnits;
}