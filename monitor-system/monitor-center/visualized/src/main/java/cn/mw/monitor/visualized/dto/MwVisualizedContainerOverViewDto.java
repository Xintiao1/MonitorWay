package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 容器大屏DTO
 * @Author gengjb
 * @Date 2023/6/7 15:07
 * @Version 1.0
 **/
@Data
@ApiModel("容器概览DTO")
public class MwVisualizedContainerOverViewDto {

    @ApiModelProperty("正常节点数量")
    private int normalNodeNumber;

    @ApiModelProperty("异常节点数量")
    private int abNormalNodeNumber;

    @ApiModelProperty("总节点数量")
    private int nodeCount;

    @ApiModelProperty("正常Pod数量")
    private int normalPodNumber;

    @ApiModelProperty("异常Pod数量")
    private int abNormalPodNumber;

    @ApiModelProperty("总Pod数量")
    private int podCount;
}
