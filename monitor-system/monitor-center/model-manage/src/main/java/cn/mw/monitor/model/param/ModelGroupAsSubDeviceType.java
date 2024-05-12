package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date
 */
@Data
public class ModelGroupAsSubDeviceType {
    @ApiModelProperty(name = "ID")
    private Integer groupId;

    @ApiModelProperty(name = "分组名称")
    private String groupName;

    @ApiModelProperty(name = "父分类ID")
    private Integer pid;

    @ApiModelProperty(name = "zabbix分组标识")
    private String network;

    @ApiModelProperty(name = "节点信息")
    private String nodes;

}
