package cn.mw.monitor.service.assets.param;

import cn.mw.monitor.service.assets.model.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/16
 */
@Data
@ToString
@Slf4j
public class AddUpdateTangAssetsParam extends ParentAssetParam implements Cloneable{
    private List<String> ids;

    private Integer instanceId;

    private Integer deviceCodeId;

    private String instanceName;

    /**
     * 主键ID
     */
    @ApiModelProperty(value = "ID")
    private String id;

    /**
     * zabbix主机分组id
     * 无
     */
    @ApiModelProperty(value = "hostGroupId")
    private String hostGroupId;

    /**
     * zabbix模版id
     */
    @ApiModelProperty(value = "templateId")
    private String templateId;
    @ApiModelProperty(value = "templateName")
    private String templateName;
    private String groupId;

    /**
     * 资产ID
     */
    @ApiModelProperty(value = "assetsId")
    private String assetsId;

    /**
     * 资产名称
     */
    @ApiModelProperty(value = "资产名称")
    private String assetsName;

    /**
     * 主机名称
     */
    @ApiModelProperty(value = "主机名称")
    private String hostName;

    /**
     * 第三方监控服务器中主机名称
     */
    @ApiModelProperty(value = "第三方监控服务器中主机名称")
    private String TPServerHostName;

    /**
     * 协议类型
     * 无
     */
    @ApiModelProperty(value = "协议类型")
    private String version;

    /**
     * 带内IP
     */
    @ApiModelProperty(value = "带内IP")
    private String inBandIp;

    /**
     * 带外IP
     */
    @ApiModelProperty(value = "带外IP")
    private String outBandIp;

    /**
     * 资产类型
     */
    @ApiModelProperty(value = "资产类型")
    private Integer assetsTypeId;

    /**
     * 资产分组名
     * 无
     */
    @ApiModelProperty(value = "资产分组名")
    private String groupTypeName;

    /**
     * 资产子类型
     */
    @ApiModelProperty(value = "资产子类型")
    private Integer assetsTypeSubId;

    /**
     * 轮训引擎
     */
    @ApiModelProperty(value = "轮训引擎")
    private String pollingEngine;
    @ApiModelProperty(value = "是否忽略code校验，默认使用")
    private boolean ignoreCodeCheck;

    //监控服务器Name
    private String monitorServerName;
    private String pollingEngineName;
    private String assetsSubTypeName;
    private String assetsTypeName;

    /**
     * 监控方式// 1 agent    2 snmp    3 port
     */
    @ApiModelProperty(value = "监控方式(1.AGENT 2.SNMP 3.JMX 4.ICMP 5.IPMI)")
    private Integer monitorMode;

    private List<Integer> monitorModeList;

    /**
     * 监控端口
     * 无
     */
    @ApiModelProperty(value = "监控端口")
    private Integer monitorPort;

    /**
     * 厂商
     */
    @ApiModelProperty(value = "厂商")
    private String manufacturer;

    /**
     * 规格型号
     */
    @ApiModelProperty(value = "规格型号")
    private String specifications;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 资产状态
     */
    @ApiModelProperty(value = "资产状态")
    private String enable;

    /**
     * 删除标识符
     */
    private Boolean deleteFlag;

    /**
     * 启动监控状态
     */
    @ApiModelProperty(value = "启动监控状态")
    private Boolean monitorFlag;

    /**
     * 启动配置状态
     */
    @ApiModelProperty(value = "启动配置状态")
    private Boolean settingFlag;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    private String sysObjId;

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;
    private List<Integer> userIds;
    @ApiModelProperty(value = "AGENT列表")
    private MwAgentAssetsDTO agentAssetsDTO;

    @ApiModelProperty(value = "PORT列表")
    private MwPortAssetsDTO portAssetsDTO;

    @ApiModelProperty(value = "IPMI列表")
    private MwIPMIAssetsDTO mwIPMIAssetsDTO;

    @ApiModelProperty(value = "SNMP等级")
    private Integer snmpLev;

    @ApiModelProperty(value = "SNMP列表")
    private MwSnmpAssetsDTO snmpAssetsDTO;

    @ApiModelProperty(value = "SNMPv1列表")
    private MwSnmpv1AssetsDTO snmpV1AssetsDTO;

    @ApiModelProperty(value = "标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty(value = "iot信息")
    private MwIOTAssetsDTO mwIOTAssetsDTO;

    @ApiModelProperty(value = "VCenter信息")
    private MwVCenterAssetsDTO mwVCenterAssetsDTO;

    @ApiModelProperty(value = "宏值")
    private List<Macros> mwMacrosDTO;
    @ApiModelProperty(value = "监控方式名称")
    private String monitorModeName;

    /**
     * 只修改启动监控状态时一个状态为3
     * 修改为1
     */
    private Integer flag = 1;

    //扫描成功表id
    private Integer scanSuccessId;

    /**
     * 监控服务器id
     */
    private Integer monitorServerId;

    /**
     * 代理agent id
     */
    private String proxyServerId;

    /**
     * 是否打开拓扑连接
     */
    private boolean openConnect;

    /**
     * 资产类型，用于进行有形资产和带外资产标签查询
     */
    private String assetsType;

    @Override
    public Object clone()  {
        AddUpdateTangAssetsParam aP = null;
        try {
            aP = (AddUpdateTangAssetsParam)super.clone();
        }catch (CloneNotSupportedException e){
            log.error("fail to clone with",e);
        }
        return aP;
    }

    /**
     * zabbix中对应模板所用的interfaces类型 /agent/JMX/IPMI/SNMP
     */
    private int interfacesType;

    /*
     * 设备id编码,snmp扫描时获取接口信息,并进行拼接
     */
    private String deviceCode;

    /**
     * 启动立即执行
     */
    @ApiModelProperty(value = "启动立即执行")
    private boolean checkNowFlag;

    private Integer addPattern;

    //是否新版本（模型管理设备资产添加）
    private Boolean isNewVersion;

    //模型实例资产是否纳管
    private Boolean isManage;

    //执行队列扫描任务时的任务ID
    private String taskId;

    //是否是预添加资产
    private Boolean ispreAddAssets;
    //是否高级设置
    private Boolean isAdvancedSetting;
    private String vxlanUser;
    private String vxlanPasswd;
    ///////// 功能模块  /////////////
    //运维监控
    private Boolean operationMonitor;
    //自动化
    private Boolean autoManage;
    //日志管理
    private Boolean logManage;
    //配置管理
    private Boolean propManage;
    //是否随机主机名称
    private boolean randomName;
}
