package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("通用二维坐标对象")
public class LogPointDTO {

    @ApiModelProperty("x轴,根据实际情况将此字段数据更改为对应类型")
    private String x;

    @ApiModelProperty("y轴")
    private Integer y;
}
