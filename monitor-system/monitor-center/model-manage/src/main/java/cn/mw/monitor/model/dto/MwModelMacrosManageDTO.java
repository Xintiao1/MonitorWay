package cn.mw.monitor.model.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/2/19
 */
@Data
@ApiModel
public class MwModelMacrosManageDTO extends BaseParam {
    @ApiModelProperty("Id")
    private Integer id;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型名称")
    private String modelName;
    @ApiModelProperty("宏值Id")
    private Integer macroId;
    @ApiModelProperty("宏值字段")
    private String macro;
    @ApiModelProperty("宏值名称")
    private String macroName;
    @ApiModelProperty("备注说明")
    private String description;
    @ApiModelProperty("宏值类型")
    private String macroType;
    @ApiModelProperty("宏值数据")
    private String macroVal;
    @ApiModelProperty("凭证名称")
    private String authName;
    @ApiModelProperty("模型宏值Id")
    private Integer modelMacroId;
    //模型groupNodes
    private String groupNodes;
    private List<Integer> modelGroupIdList;

}
