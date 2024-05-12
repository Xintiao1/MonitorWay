package cn.mw.monitor.service.model.param;

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
public class MwModelInstanceParam {
    @ApiModelProperty("模型实例id")
    private Integer instanceId;
    private int relationInstanceId;
    private String esId;
    @ApiModelProperty("模型实例名称")
    private String instanceName;
    private String modelIndex;
    private String modelId;
    private String modelName;
    private int modelView;
    @ApiModelProperty("是否纳管")
    private Boolean isManage;
    @ApiModelProperty("纳管资产Id")
    private String assetsId;
    @ApiModelProperty("纳管资产监控方式")
    private Integer monitorMode;
    private int monitorServerId;
    private String propertiesIndex;
    private Object propertiesVal;
    private Integer propertiesType;
    //web监测clientid
    private String httpId;
}
