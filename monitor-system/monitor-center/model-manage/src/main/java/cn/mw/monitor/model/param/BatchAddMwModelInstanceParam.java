package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2023/5/13 17:10
 */
@Data
@ApiModel
public class BatchAddMwModelInstanceParam {
    @ApiModelProperty("资产名称")
    private String instanceName;
    @ApiModelProperty("IP")
    private String inBandIp;
    @ApiModelProperty("实例Id")
    private Integer modelInstanceId;
}
