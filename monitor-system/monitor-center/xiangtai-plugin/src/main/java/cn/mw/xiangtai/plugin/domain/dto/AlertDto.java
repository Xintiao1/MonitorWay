package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 告警DTO
 * @date 2023/10/23 11:13
 */
@ApiModel("告警DTO")
@Data
public class AlertDto {

    @ApiModelProperty("告警等级")
    private Integer alertLevel;

    @ApiModelProperty("告警等级名称")
    private String alertLevelName;

    @ApiModelProperty("告警数量")
    private Integer alertCount;
}
