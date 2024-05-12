package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName
 * @Description 中控机柜监测DTO
 * @Author gengjb
 * @Date 2023/3/16 11:25
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwVisuZkSoftWareSurveyDto {

    @ApiModelProperty("机柜名称")
    private String serverRoomName;

    @ApiModelProperty("温度(上)")
    private String temperatureUp;

    @ApiModelProperty("温度(下)")
    private String temperatureDown;

    @ApiModelProperty("噪声(上)")
    private String noiseUp;

    @ApiModelProperty("噪声(下)")
    private String noiseDown;

    @ApiModelProperty("湿度(上)")
    private String humidityUp;

    @ApiModelProperty("湿度(下)")
    private String humidityDown;

    @ApiModelProperty("前门")
    private String frontDoor;

    @ApiModelProperty("后门")
    private String behindDoor;


}
