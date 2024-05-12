package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 健康评分DTO
 * @Author gengjb
 * @Date 2023/4/18 14:49
 * @Version 1.0
 **/
@Data
@ApiModel("健康评分DTO")
public class MwVisualizedModuleHealthScoreDto {

    @ApiModelProperty("总评分")
    private Integer countScore;

    @ApiModelProperty("时间明细评分")
    private List<HealthScoreDetailedDto> scoreDetailedDtos;
}
