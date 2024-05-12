package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelListInfoDTO {

    @ApiModelProperty(value = "modelIndex")
    private String modelIndex;

    @ApiModelProperty(value = "模型名称")
    private String modelName;

    @ApiModelProperty(value = "模型Id")
    private String modelId;

    @ApiModelProperty(value = "模型描述")
    private String modelDesc;

    @ApiModelProperty(value = "模型类型Id")
    private Integer modelTypeId;

    @ApiModelProperty(value = "模型类型名称")
    private String modelTypeName;

    @ApiModelProperty(value = "模型实例数量")
    private Integer instanceNum;

    @ApiModelProperty(value = "模型创建时间")
    private String creatDate;

    @ApiModelProperty(value = "创建人")
    private String creator;

    private String type;
    private String pids;
    //为了符合实例列表树结构的数据，故命名为此
    private String modelGroupIdStr;
    private String modelGroupId;

    private Boolean isBase;

}
