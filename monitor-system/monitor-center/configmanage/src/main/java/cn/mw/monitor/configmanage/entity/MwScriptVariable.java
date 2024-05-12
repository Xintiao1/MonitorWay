package cn.mw.monitor.configmanage.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 2023427 10:01
 * @description
 */
@Data
@ApiModel("系统变量 ")
public class MwScriptVariable {
    @ApiModelProperty(value="主键")
    private Integer id;
    @ApiModelProperty(value="变量名称")
    private String name;

    @ApiModelProperty(value="变量种类 0.字符串 1.主机")
    private Integer type;

    @ApiModelProperty(value="初始化值")
    private String value;

    @ApiModelProperty(value="赋值可变 0 false 1 ture")
    private Integer changeType;

    @ApiModelProperty(value="执行前确定值 0 false 1 ture")
    private Integer runTure;

    @ApiModelProperty(value="变量类型 0.作业变量 1.执行方案变量")
    private Integer varibleType;

    @ApiModelProperty(value="变量描述")
    private String varibleDesc;


    @ApiModelProperty(value="关联id")
    private Integer conrrelationId;

}
