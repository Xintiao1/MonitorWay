package cn.mw.monitor.timetask.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lumingming
 * @createTime 24 16:52
 * @description
 */
@Data
@ApiModel("定时任务动作")
public class TimetaskActrion {
    @ApiModelProperty("tree_id")
    private String id ;
    @ApiModelProperty("动作名称")
    private String actionName ;
    @ApiModelProperty("模块id")
    private Integer modelId ;
    @ApiModelProperty("树名称")
    private String treeName ;

    @ApiModelProperty("实现的位置")
    private String actionImpl ;

    @ApiModelProperty("动作实现方法")
    private String actionMethod ;

    @ApiModelProperty("动作实现存在指定对象")
    private String actionModel ;
}
