package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 模型类型DTO
 * @Author gengjb
 * @Date 2023/4/17 22:56
 * @Version 1.0
 **/
@Data
@ApiModel("模型类型DTO")
public class MwVisualizedModuleModelTypeDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("类型实例数量")
    private Integer typeCount;

    @ApiModelProperty("状态")
    private String status;
}
