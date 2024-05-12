package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 可视化下拉数据DTO
 * @Author gengjb
 * @Date 2023/5/21 15:36
 * @Version 1.0
 **/
@Data
@ApiModel("可视化下拉数据DTO")
public class MwVisualizedDropDownDto {

    @ApiModelProperty("下拉值")
    private String dropValue;

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("监控名称")
    private String itemName;
}
