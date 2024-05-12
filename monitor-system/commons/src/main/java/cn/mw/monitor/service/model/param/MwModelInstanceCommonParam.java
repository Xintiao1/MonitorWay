package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/03/25 10:14
 */
@Data
@ApiModel
public class MwModelInstanceCommonParam {
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型实例id")
    private Integer modelInstanceId;
    private List<Integer> instanceIdList;
    @ApiModelProperty("模型实例名称")
    private String modelInstanceName;
    @ApiModelProperty("模型索引")
    private String modelIndex;
    @ApiModelProperty("模型属性索引")
    private String propertiesIndexId;
    @ApiModelProperty("关联实例id")
    private Integer relationInstanceId;
    @ApiModelProperty("关联实例id")
    private String relationInstanceName;
    private String cabinetPosition;
    private String groupNodes;
    private String groupId;
    private String modelView;

    private String modelDesc;

}
