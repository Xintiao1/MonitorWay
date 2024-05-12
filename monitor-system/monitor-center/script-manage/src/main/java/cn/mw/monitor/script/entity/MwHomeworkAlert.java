package cn.mw.monitor.script.entity;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

;

/**
 * @author lumingming
 * @createTime 2023502 10:56
 * @description
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "告警触发作业")
public class MwHomeworkAlert extends BaseParam {

    @ApiModelProperty("主键ID")
    private Integer id;

    /**
     * 告警触发的名称
     */
    @ApiModelProperty("告警触发的名称")
    private String alertPlanName;

    /**
     * 告警标题
     */
    @ApiModelProperty("告警标题")
    private String alertTitle;

    /**
     * 告警等级
     */
    @ApiModelProperty("告警等级")
    private Integer alertLevel;

    /**
     * 作业所在树ID
     */
    @ApiModelProperty("告警执行")
    private String alertExeHomework;

    /**
     * 告警触发次数
     */
    @ApiModelProperty("告警触发次数")
    private Integer alertTriggerNum;

    /**
     * 告警触发类型
     */
    @ApiModelProperty("告警触发类型")
    private Integer alertType;


    /**
     * 告警触发类型
     */
    @ApiModelProperty("告警触发类型")
    private Integer titleCheck;
}
