package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@ApiModel
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwCustomFieldCommon {

    @ApiModelProperty(value = "模型id")
    private Integer modelId;

    @ApiModelProperty(value = "模型ids")
    private List<MWModelInstanceFiled> fieldInfo;

    @ApiModelProperty(value = "查询类型:insert,list,editor,look")
    private String queryType;

}
