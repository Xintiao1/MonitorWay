package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @date 2024/1/2 10:18
 */
@Data
public class ItemTempAndHumidityDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("温度")
    private String temperature;

    @ApiModelProperty("湿度")
    private String humidity;

    @ApiModelProperty("ups")
    private String ups;

}
