package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 可视化图片信息DTO
 * @date 2023/11/6 15:10
 */
@Data
@ApiModel("可视化图片信息DTO")
public class MwVisualizedImageDto {

    @ApiModelProperty("视图ID")
    private Integer visualizedId;

    @ApiModelProperty("前端nodeId")
    private String nodeId;

    @ApiModelProperty("图片地址")
    private String imageUrl;
}
