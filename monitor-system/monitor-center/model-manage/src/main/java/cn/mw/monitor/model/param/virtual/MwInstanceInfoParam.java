package cn.mw.monitor.model.param.virtual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/9/15
 */
@Data
public class MwInstanceInfoParam {
    @ApiModelProperty("模型索引")
    private String modelIndex;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型实例Id")
    private Integer modelInstanceId;
    @ApiModelProperty("设备名称")
    private String instanceName;
    @ApiModelProperty("esId")
    private String esId;
    @ApiModelProperty("主机id")
    private String assetsId;
    private String id;
    private String type;
    private String pId;
    private String clusterId;
    private String datacenterId;
}
