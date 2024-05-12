package cn.huaxing.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 可视化参数
 * @date 2023/9/4 9:41
 */
@Data
@ApiModel("可视化参数")
public class HuaxingVisualizedParam {

    @ApiModelProperty("图类型")
    private Integer chartType;

    @ApiModelProperty("分区名称")
    private String partitionName;
}
