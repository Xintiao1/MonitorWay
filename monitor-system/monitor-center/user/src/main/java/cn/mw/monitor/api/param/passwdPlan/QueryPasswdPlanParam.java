package cn.mw.monitor.api.param.passwdPlan;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("查询密码策略列表数据")
public class QueryPasswdPlanParam extends BaseParam {

    // 策略id
    @ApiModelProperty("密码策略id")
    private Integer passwdId;
    // 策略名称
    @ApiModelProperty("密码策略名称")
    private String passwdName;
    // 策略状态
    @ApiModelProperty("密码策略状态")
    private String passwdState;
    // 创建人
    @ApiModelProperty("创建人")
    private String creator;
    // 修改人
    @ApiModelProperty("修改人")
    private String modifier;
    // 创建时间开始
    @ApiModelProperty("创建时间开始")
    private Date createTimeStart;
    // 创建时间结束
    @ApiModelProperty("创建时间结束")
    private Date createTimeEnd;
    // 修改时间开始
    @ApiModelProperty("修改时间开始")
    private Date updateTimeStart;
    // 修改时间结束
    @ApiModelProperty("修改时间结束")
    private Date updateTimeEnd;

}
