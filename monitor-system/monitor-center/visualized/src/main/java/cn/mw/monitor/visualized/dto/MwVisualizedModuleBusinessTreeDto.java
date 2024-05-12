package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 组件绑定业务树结构
 * @Author gengjb
 * @Date 2023/4/21 14:39
 * @Version 1.0
 **/
@Data
@ApiModel("组件绑定业务树结构")
public class MwVisualizedModuleBusinessTreeDto {

    @ApiModelProperty("业务名称")
    private String name;

    @ApiModelProperty("业务字段")
    private String indexId;

    @ApiModelProperty("业务ID")
    private Integer typeId;

    @ApiModelProperty("是否可以选择")
    private boolean disabled;

    @ApiModelProperty("唯一标识")
    private String uuid;

    @ApiModelProperty("数据ID")
    private String id;

    @ApiModelProperty("子业务")
    private List<MwVisualizedModuleBusinessTreeDto> children;
}
