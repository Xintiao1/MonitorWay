package cn.mw.monitor.service.assets.model;

import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.beans.BeanUtils;
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

    @ApiModelProperty(value="轮询引擎的状态")
    private String pollingMode;

    private String specifications;

    @ApiModelProperty(value="模型区域")
    private String modelArea;

    @ApiModelProperty(value="模型业务系统")
    private String modelSystemName;

    @ApiModelProperty(value="模型标签")
    private String modelTag;

    private String instanceName;

    private Boolean isQueryAssetsState;

    public String debugInfo(){
        return super.toString();
    }

    public void extractFrom(AddUpdateTangAssetsParam addUpdateTangAssetsParam){
        BeanUtils.copyProperties(addUpdateTangAssetsParam ,this);
    }
}
