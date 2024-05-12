package cn.mw.monitor.timetask.entity;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author lumingming
 * @createTime 03 14:36
 * @description
 */
@Data
@ApiModel("修改定时任务")
public class NewTimeTaskEditor extends BaseParam implements Serializable {
    public static final long serialVersionUID = 1L;
    @ApiModelProperty("计划组id")
    public String id ;
    @ApiModelProperty("模块id")
    public Integer modelId ;
    @ApiModelProperty("动作id")
    public String actionId ;
    @ApiModelProperty("时间计划名称")
    public String timeName ;
    @ApiModelProperty("时间计划描述")
    public String timeDescription ;
    @ApiModelProperty("当前周期")
    public String cycle ;
    @ApiModelProperty("时间计划按钮")
    public boolean timeButton;
    @ApiModelProperty("时间计划其他规定参数")
    public String timeObject ;
    @ApiModelProperty("下次开始时间")
    public Date timeStartTime ;
    @ApiModelProperty("上次完成时间")
    public Date timeEndTime ;
    @ApiModelProperty("关联动作")
    public String timeAction ;
    @ApiModelProperty("关联模块")
    public String timeModel ;
    @ApiModelProperty("绑定的模块里的对象")
    List<Tree> newtimetaskMapperObjects;
    @ApiModelProperty("绑定模块里的时间")
    List<MwNcmTimetaskTimePlan> newtimetaskMapperTimes;
}
