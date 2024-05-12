package cn.mw.monitor.service.assets.model;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;


/**
 * Created by baochengbin on 2020/3/12.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@ApiModel(value = "资产查询ById返回结果集")
public class MwTangibleassetsByIdDTO extends MwTangibleassetsTable {
    @ApiModelProperty(value="snmpv1/v2")
    private MwSnmpv1AssetsDTO snmpv1AssetsDTO;

    @ApiModelProperty(value="snmp集合")
    private MwSnmpAssetsDTO snmpAssetsDTO;

    @ApiModelProperty(value="agent接口")
    private MwAgentAssetsDTO agentAssetsDTO;

    @ApiModelProperty(value="端口扫描")
    private MwPortAssetsDTO portAssetsDTO;

    @ApiModelProperty(value="标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty(value="机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value="负责人")
    private List<Integer> principal;

    @ApiModelProperty(value="用户组")
    private List<Integer> groupIds;

    @ApiModelProperty(value="未处理机构")
    private List<OrgDTO> department;

    @ApiModelProperty(value="轮询引擎的状态")
    private String pollingMode;

    @ApiModelProperty(value = "用户组ID对应的MAP集合")
    private List<GroupDTO> groupIdsMap;

    @ApiModelProperty(value = "用户ID对应的MAP集合")
    private List<UserDTO> principalMap;
}
