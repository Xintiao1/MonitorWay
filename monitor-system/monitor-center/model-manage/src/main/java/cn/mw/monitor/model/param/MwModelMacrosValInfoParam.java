package cn.mw.monitor.model.param;

import cn.mw.monitor.model.type.MwModelMacrosValInfoParamType;
import cn.mw.monitor.service.dropdown.param.SelectCharDropDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzg
 * @date 2023/2/19
 */
@Data
@ApiModel
public class MwModelMacrosValInfoParam {
    @ApiModelProperty("Id")
    private Integer id;
    @ApiModelProperty("模型Id")
    private Integer modelId;
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

    @ApiModelProperty("模型宏值agent下拉框")
    private List<MwModelMacrosValSelParam> agentList;
    private String groupNodes;

    public void extractFrom(List<SelectCharDropDto> selectCharDropDtos){
        this.id = 0;
        agentList = new ArrayList<>();
        for(SelectCharDropDto selectCharDropDto : selectCharDropDtos){
            MwModelMacrosValSelParam mwModelMacrosValSelParam = new MwModelMacrosValSelParam();
            mwModelMacrosValSelParam.setLabel(selectCharDropDto.getDropValue());
            mwModelMacrosValSelParam.setValue(selectCharDropDto.getDropKey());
            agentList.add(mwModelMacrosValSelParam);
        }
        this.macroType = MwModelMacrosValInfoParamType.EngineSel.getCode();
        this.macro = MwModelMacrosValInfoParamType.EngineSel.getName();
        this.macroName = MwModelMacrosValInfoParamType.EngineSel.getDesc();
        this.macroVal = agentList.get(0).getValue();
        this.modelMacroId = 0;
        this.macroId = 0;
    }

}
