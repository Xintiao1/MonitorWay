package cn.mw.monitor.model.param;

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
public class BatchDeleteModelInstanceParam {
    @ApiModelProperty("模型实例id")
    private Integer instanceId;
    private String esId;
    @ApiModelProperty("模型实例名称")
    private String instanceName;
    private String modelIndex;
    private String modelId;
    @ApiModelProperty("是否纳管")
    private Boolean isManage;
    @ApiModelProperty("纳管资产Id")
    private String assetsId;
    @ApiModelProperty("纳管资产监控方式")
    private Integer monitorMode;
    private int monitorServerId;
    @ApiModelProperty("是否转移")
    private Boolean isShift;
    @ApiModelProperty("模型视图类型，0：普通，1：机房，2机柜")
    private Integer modelViewType;
    //依附的实例id
    private Integer relationInstanceId;
    @ApiModelProperty("外部关联modelIndex")
    private String relationModelIndex;

}
