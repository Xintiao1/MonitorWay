package cn.mw.monitor.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author lumingming
 * @createTime 08 10:38
 * @description
 */
@Data
@ApiModel
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TimeTaskRresult {

    // 每页显示行数
    @ApiModelProperty("0.String 1.url")
    private Integer resultType;


    @ApiModelProperty("历史id，不需要传")
    private Integer id;

    @ApiModelProperty("返回url链接成功信息结果")
    private String resultContext;

    @ApiModelProperty("返回执行对象名称")
    private String objectName;

    @ApiModelProperty("返回执行动作名称")
    private String actionName;

    @ApiModelProperty("返回执行模块名称")
    private String actionModel;

    @ApiModelProperty("进入时间")
    private Date startTime = new Date();

    @ApiModelProperty("返回成功或失败")
    private boolean isSuccess = false;

    @ApiModelProperty("失败原因")
    private String failReason;


    @ApiModelProperty("方法类补填参数（不用传）：返回时间")
    private Date endTime;
    @ApiModelProperty("计算总时间（不用传）：s为单位")
    private Integer runTime;
    @ApiModelProperty("关联对象（不用传）：s为单位")
    private String objectId;
    @ApiModelProperty("关联定时计划（不用传）：id")
    private String newTimetaskId;

    public void setResultEndDate(Date s,Date resultEndDate) {
        Long st =(resultEndDate.getTime()-s.getTime())/1000;
        this.runTime = st.intValue();
        this.endTime = resultEndDate;
    }
}
