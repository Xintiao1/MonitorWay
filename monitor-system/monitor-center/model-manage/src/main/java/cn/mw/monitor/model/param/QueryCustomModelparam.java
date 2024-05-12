package cn.mw.monitor.model.param;

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
public class QueryCustomModelparam {

    @ApiModelProperty(value = "用户id")
    private Integer userId;

    @ApiModelProperty(value = "模型id")
    private Integer modelId;

    @ApiModelProperty(value = "模型pids")
    private String pids;

    @ApiModelProperty(value = "模型分类")
    private String propertiesType;

    private String treeType;

    @ApiModelProperty(value = "查询类型:insert,list,editor,look")
    private String queryType;

}
