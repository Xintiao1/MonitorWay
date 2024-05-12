package cn.mw.monitor.timetask.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 24 16:50
 * @description
 */

@Data
@ApiModel("定时任务模块")
public class TimetaskModel {
    // 第几页
    @ApiModelProperty("id")
    private Integer id ;
    @ApiModelProperty("模块名称")
    private String modelName ;
    @ApiModelProperty("模块类型 0.自定义 1.系统")
    private Integer modelType ;
    @ApiModelProperty("模块所带动作")
    private List<TimetaskActrion> timetaskActrions;
    @ApiModelProperty("节点名称")
    private String treeName;
    @ApiModelProperty("模块ID")
    private Integer modelId;
}
