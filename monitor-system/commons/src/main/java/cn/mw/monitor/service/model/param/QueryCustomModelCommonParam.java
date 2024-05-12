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
public class QueryCustomModelCommonParam {

    @ApiModelProperty(value = "用户id")
    private Integer userId;

    @ApiModelProperty(value = "模型ids")
    private List<Integer> modelIds;

    @ApiModelProperty(value = "查询类型:insert,list,editor,look")
    private String queryType;

}
