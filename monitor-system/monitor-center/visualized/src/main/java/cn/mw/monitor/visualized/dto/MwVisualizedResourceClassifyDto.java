package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 资源统计分类
 * @Author gengjb
 * @Date 2023/5/17 10:12
 * @Version 1.0
 **/
@Data
@ApiModel("资源统计DTO")
public class MwVisualizedResourceClassifyDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("值")
    private Integer value;

    @ApiModelProperty("值单位")
    private String units;

    @ApiModelProperty("已使用率")
    private Double use;

    @ApiModelProperty("使用率单位")
    private String useUnits;
}
