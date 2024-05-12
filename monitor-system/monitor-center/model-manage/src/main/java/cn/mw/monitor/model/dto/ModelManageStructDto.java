package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/7 9:51
 */
@Data
public class ModelManageStructDto {
    @ApiModelProperty("模型Id")
    private Integer modelId;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("模型描述")
    private String modelDesc;

    @ApiModelProperty("模型索引")
    private String modelIndex;

    @ApiModelProperty("模型级别 0:内置模型，1:自定义模型;其中内置模型不可删除")
    private Integer modelLevel;

    @ApiModelProperty("模型分组节点")
    private String groupNodes;

    @ApiModelProperty("父模型ids ")
    private String pids;

    private List<ModelPropertiesStructDto> propertiesStruct;
}
