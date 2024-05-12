package cn.mw.monitor.timetask.entity;

import cn.mw.monitor.bean.BaseParam;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author lumingming
 * @createTime 03 14:36
 * @description
 */
@Data
@ApiModel("新定时任务")
public class NewTimeTask extends BaseParam implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("计划组id")
    private String id ;
    @ApiModelProperty("模块id")
    @NotNull(message = "modelId不能为空")
    private Integer modelId ;
    @ApiModelProperty("动作id")
    @NotNull(message = "动作不能为空")
    private String actionId ;
    @ApiModelProperty("时间计划名称")
    @NotNull(message = "时间计划名称不能为空")
    private String timeName ;
    @ApiModelProperty("当前周期")
    private String cycle ;
    @ApiModelProperty("时间计划描述")
    private String timeDescription ;
    @ApiModelProperty("时间计划按钮")
    private Integer timeButton;
    @ApiModelProperty("时间计划其他规定参数")
    private String timeObject ;
    @ApiModelProperty("下次开始时间")
    private Date timeStartTime ;
    @ApiModelProperty("完成执行的时间 单位：s")
    private Integer timeCount ;
    @ApiModelProperty("上次完成时间开始时间")
    private Date timeStartTimeStart ;
    @ApiModelProperty("上次完成时间结束时间")
    private Date timeStartTimeEnd ;
    @ApiModelProperty("下次开始时间开始时间")
    private Date timeEndTimeStart ;
    @ApiModelProperty("下次开始时间结束时间")
    private Date timeEndTimeEnd ;
    @ApiModelProperty("上次完成时间")
    private Date timeEndTime ;
    @ApiModelProperty("关联动作")
    private String timeAction ;
    @ApiModelProperty("关联模块")
    private String timeModel ;
    @ApiModelProperty("绑定的模块里的对象")
    @Size(min = 1,message = "绑定的模块里的对象不能为空")
    List<NewtimetaskMapperObject> newtimetaskMapperObjects;
    @ApiModelProperty("绑定模块里的时间")
    @Size(min = 1,message = "绑定模块里的时间不能为空")
    List<NewtimetaskMapperTime> newtimetaskMapperTimes;
}
