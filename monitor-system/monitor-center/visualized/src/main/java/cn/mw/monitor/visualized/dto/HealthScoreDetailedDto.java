package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName
 * @Description 健康评分明细DTO
 * @Author gengjb
 * @Date 2023/4/18 14:57
 * @Version 1.0
 **/
@Data
@ApiModel("健康评分DTO")
public class HealthScoreDetailedDto {

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("时间，单位：秒")
    private String clock;

    @ApiModelProperty("评分")
    private Integer score;
}
