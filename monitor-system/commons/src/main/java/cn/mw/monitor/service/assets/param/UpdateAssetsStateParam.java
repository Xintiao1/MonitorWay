package cn.mw.monitor.service.assets.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/24
 */
@Data
@ApiModel(value = "更新资产状态数据")
public class UpdateAssetsStateParam {

    @ApiModelProperty("id列表")
    private List<String> idList;

    @ApiModelProperty("主机Id列表")
    private List<String> hostIds;

    @ApiModelProperty("主机Name列表")
    private List<String> hostNames;

    @ApiModelProperty("状态类型")
    private String stateType;

    @ApiModelProperty("enable")
    private String enable;

    @ApiModelProperty("监控服务器Id")
    private Integer monitorServerId;

    @ApiModelProperty("监控方式")
    private Integer monitorMode;

}
