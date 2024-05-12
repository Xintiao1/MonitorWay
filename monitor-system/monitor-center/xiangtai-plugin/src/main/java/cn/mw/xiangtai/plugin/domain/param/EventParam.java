package cn.mw.xiangtai.plugin.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("日志事件总数")
public class EventParam {

    /**
     * 下面选类型后，这里传间隔时间
     */
    @ApiModelProperty("间隔时间 默认5")
    private Integer interval = 5;

    private String unit;

    /**
     * 1 : 五年
     * 2 : 12个月
     * 3 : 30天
     */
    private Integer dateType = 1;

}
