package cn.mw.xiangtai.plugin.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 祥泰大屏参数
 * @date 2023/10/16 10:06
 */
@Data
@ApiModel("祥泰大屏参数")
public class XiangtaiVisualizedParam {

    @ApiModelProperty("组件ID")
    private Integer chartType;

    @ApiModelProperty("展示几条")
    private Integer topN;

    @ApiModelProperty("日期类型 1：按年，2：按月，3：按日")
    private Integer dateType;

    @ApiModelProperty("间隔时间")
    private Integer interval;
}
