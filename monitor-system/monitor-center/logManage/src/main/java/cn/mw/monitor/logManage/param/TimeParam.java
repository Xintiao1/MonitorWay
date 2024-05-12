package cn.mw.monitor.logManage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gui.quanwang
 * @className TImeParam
 * @description 时间类型
 * @date 2023/4/6
 */
@Data
@ApiModel("时间类型参数")
public class TimeParam {

    /**
     * 时间类型（绝对值）
     */
    public final static String DATE_TYPE_ABSOLUTE = "A";
    /**
     * 当前时间
     */
    public final static String DATE_TYPE_NOW = "N";
    /**
     * 相对时间
     */
    public final static String DATE_TYPE_RELATIVE = "R";


    /**
     * 时间类型（3类，A：绝对时间 N：当前时间 R：相对时间）
     */
    @ApiModelProperty("时间类型")
    private String type;

    /**
     * 时间单位（type=R  生效）
     */
    @ApiModelProperty("时间单位")
    private String unit;

    /**
     * 时间值（type=A or type=R 时生效， type=A时为时间戳， type=R时为具体数值）
     */
    @ApiModelProperty("时间值")
    private String value;

    /**
     * 是否抹零(支持分、秒、时、天、周、月、年)
     */
    @ApiModelProperty("是否抹零")
    private boolean round;

    /**
     * 前端展示时间，不做处理
     */
    @ApiModelProperty("前端展示时间")
    private String roundForTime;
}
