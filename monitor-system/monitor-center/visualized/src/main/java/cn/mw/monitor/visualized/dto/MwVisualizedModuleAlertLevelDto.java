package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName
 * @Description 告警等级DTO
 * @Author gengjb
 * @Date 2023/4/17 14:15
 * @Version 1.0
 **/
@Data
@ApiModel("告警等级DTO")
public class MwVisualizedModuleAlertLevelDto {

    @ApiModelProperty("告警等级名称")
    private String levelName;

    @ApiModelProperty("告警数量")
    private Integer alertCount;

    @ApiModelProperty("告警等级排序标识")
    private String sortLevelType;

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("排序时间")
    private Date sortDate;

}
