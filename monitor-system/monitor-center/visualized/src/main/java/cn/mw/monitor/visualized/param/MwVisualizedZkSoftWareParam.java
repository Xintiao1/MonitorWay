package cn.mw.monitor.visualized.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 可视化中控参数
 * @Author gengjb
 * @Date 2023/3/15 16:20
 * @Version 1.0
 **/
@Data
@ApiModel("可视化中控参数")
public class MwVisualizedZkSoftWareParam {

    @ApiModelProperty("图类型")
    private Integer chartType;

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("时间类型")
    private Integer dateType;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;
}
