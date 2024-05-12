package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 评分状态DTO
 * @date 2023/9/27 14:50
 */
@Data
@ApiModel("评分占比DTO")
public class MwVisualizedScoreStatusDto {

    @ApiModelProperty("时间")
    private String clock;

    @ApiModelProperty("正常数量")
    private int normalCount;

    @ApiModelProperty("异常数量")
    private int abNormalCount;

    @ApiModelProperty("评分")
    private double scorce;
}
