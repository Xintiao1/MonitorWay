package cn.mw.monitor.service.visualized.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 数字孪生监控信息查询参数实体
 * @date 2023/8/2 14:29
 */
@Data
@ApiModel("数字孪生监控信息参数")
public class MwDigitalTwinItemParam {

    @ApiModelProperty("服务器ID")
    private Integer serverId;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("资产主键ID")
    private String assetsId;

    @ApiModelProperty("资产主键ID集合")
    private List<String> assetsIds;

    @ApiModelProperty("资产主机ID集合")
    private List<String> hostIds;
}
