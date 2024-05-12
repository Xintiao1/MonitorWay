package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.param.MwModelInstanceParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/25 9:26
 */
@ApiModel
@Data
public class DeleteModelInstanceParamV2 {
    @ApiModelProperty("es的索引")
    private String modelIndex;
    private List<Long> instanceIds;

    private List<String> modelIndexs;

    private Long modelId;

    private List<String> esIdList;
    @ApiModelProperty("模型名称")
    private String modelName;
    private List<MwModelInstanceParam> paramList;
    @ApiModelProperty("是否转移")
    private Boolean isShift;
    @ApiModelProperty("模型视图类型，0：普通，1：机房，2机柜")
    private Integer modelViewType;
    @ApiModelProperty("外部关联modelIndex")
    private String relationModelIndex;

    //依附的实例id
    private Long relationInstanceId;
}
