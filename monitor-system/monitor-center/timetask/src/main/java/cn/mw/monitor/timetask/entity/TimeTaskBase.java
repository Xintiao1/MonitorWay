package cn.mw.monitor.timetask.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lumingming
 * @createTime 08 14:20
 * @description
 */
@Data
@ApiModel("定时任务")
public class TimeTaskBase {

    // 第几页
    @ApiModelProperty("type 0.表示自定义定时任务 1.表示系统定时任务")
    private Integer type = 0;

    @ApiModelProperty("是否需要模块：默认0不需要")
    private Integer modelId = 0;
    @ApiModelProperty("是否需要动作:true")
    private boolean actionHave = true;
    @ApiModelProperty("查询相对应")
    private String sreach;
}