package cn.mw.monitor.configmanage.entity;


import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
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
@ApiModel(value = "资产返回结果集")
public class MwTangibleassetsDTO extends MwTangibleassetsTable {

    /**
     * 轮询引擎
     */
    @ApiModelProperty(value="轮询引擎")
    private String pollingEngineName;

    /**
     * 监控方式
     */
    @ApiModelProperty(value="监控方式")
    private String monitorModeName;

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
    private List<OrgDTO> department;

    @ApiModelProperty(value="负责人")
    private List<UserDTO> principal;

    @ApiModelProperty(value="用户组")
    private List<GroupDTO> group;
}
