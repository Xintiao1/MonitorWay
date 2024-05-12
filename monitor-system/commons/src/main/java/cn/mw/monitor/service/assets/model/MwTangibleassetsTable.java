package cn.mw.monitor.service.assets.model;

import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.model.dto.BaseModelInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author baochengbin
 * @date 2020/03/16
 *
 */
@Data
public class MwTangibleassetsTable extends BaseModelInfo {

    private String tpServerHostName;
    /**
     * 自增主键
     */
    private String id;

    /**
     * 资产id
     */
    private String assetsId;

    /**
     * 资产名称
     */
    private String assetsName;

    /**
     * 主机名称
     */
    private String hostName;

    private String accountId;
    private String accountAlias;

    /**
     * 带内IP
     */
    private String inBandIp;

    /**
     * 带外IP
     */
    private String outBandIp;

    /**
     * 资产类型
     */
    private Integer assetsTypeId;

    /**
     * 资产类型名称
     */
    private String assetsTypeName;

    /**
     * 资产子类型
     */
    private Integer assetsTypeSubId;

    /**
     * 资产子类型名
     */
    private String assetsTypeSubName;

    /**
     * 轮训引擎
     */
    private String pollingEngine;

    /**
     * 监控方式
     */
    private Integer monitorMode;
    private String monitorModeName;

    @ApiModelProperty(value = "snmp协议等级 1:1-2级 2：3级")
    private Integer snmpLev;

    /**
     * 厂商
     */
    private String manufacturer;

    /**
     * 规格型号
     */
    private String specifications;

    /**
     * 描述
     */
    private String description;

    /**
     * 资产状态
     */
    private String enable;

    /**
     * 删除标识符
     */
    private Boolean deleteFlag;

    /**
     * 启动监控状态
     */
    private Boolean monitorFlag;

    /**
     * 启动配置状态
     */
    private Boolean settingFlag;

    /**
     * 扫描成功表id
     */
    private Integer scanSuccessId;

    /**
     * 服务器id
     */
    private Integer monitorServerId;
    private String monitorServerName;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    private String timing;
//    //厂商对应的大图标和小图标
//    private VendorIconDTO vendorIconDTO;
    /**
     * 品牌小图标
     */
    private String vendorSmallIcon;

    private Integer vendorCustomFlag;

    private String templateId;

    private String itemAssetsStatus;

    /**
     * 机构名称
     */
    private String orgName;

    private List<OrgDTO> department;

    private List<GroupDTO> group;


    /**
     * 自定义字段
     */
    private Map<String,String> customFieldValue;

    /**
     * 是否打开拓扑连接
     */
    private boolean openConnect;

    /**
     * 轮询引擎
     */
    @ApiModelProperty(value = "轮询引擎")
    private String pollingEngineName;
    @ApiModelProperty(value = "轮询引擎本机还是自定义")
    private String rngineradio;

    public void setPollingEngine(String pollingEngine) {
        if (pollingEngine == null || "".equals(pollingEngine)) {
            this.rngineradio = "本机";
        } else {
            this.rngineradio = "自定义";
        }
        this.pollingEngine = pollingEngine;
    }

    private Boolean isJump = true;

    private List<QueryAssetsInterfaceParam> interfaceList;

    private String vxlanUser;
    private String vxlanPasswd;

    private List<Integer> modelViewUserIds;

    private List<Integer> modelViewGroupIds;
    private List<List<Integer>> modelViewOrgIds;

    public void setModelInstanceId(Integer modelInstanceId) {
        super.setModelInstanceId(modelInstanceId);
        this.id = modelInstanceId==null?id:modelInstanceId.toString();
    }

    public void setInstanceName(String instanceName) {
        super.setInstanceName(instanceName);
        this.assetsName = instanceName==null?assetsName:instanceName;
    }


    public String debugInfo(){
        return "MwTangibleassetsTable{" +
                "id=" + id + + '\'' +
                ",assetsId='" + assetsId + '\'' +
                ", assetsName='" + assetsName + '\'' +
                ", inBandIp='" + inBandIp + '\'' +
                '}';
    }
}
