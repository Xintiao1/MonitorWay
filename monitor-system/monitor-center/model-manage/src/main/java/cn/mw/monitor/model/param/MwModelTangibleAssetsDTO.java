package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/12/12
 */
@Data
public class MwModelTangibleAssetsDTO {

    private String id;
    /**
     * 资产id
     */
    private String assetsId;

    private Integer modelInstanceId;

    private Integer modelId;

    /**
     * 资产名称(assetsName)
     */
    private String instanceName;

    /**
     * 资产名称(assetsName)
     */
    private String instanceId;

    /**
     * 主机名称
     */
    private String hostName;

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
     * 资产子类型
     */
    private Integer assetsTypeSubId;

    private String groupNodes;


    /**
     * 轮训引擎
     */
    private String pollingEngine;

    /**
     * 监控方式
     */
    private String monitorMode;

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
     * 启动监控状态
     */
    private Boolean monitorFlag;

    //服务器资产名称
    private String TPServerHostName;

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

    private String vendorSmallIcon;

    private Integer vendorCustomFlag;

    private String templateId;

    private String itemAssetsStatus;

    /**
     * 机构名称
     */
    private String orgName;

    private String modelIndex;


    /**
     * 自定义字段
     */
    private Map<String,String> customFieldValue;


    /**
     * 是否打开拓扑连接
     */
    private boolean openConnect;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    private String vxlanUserName;
    private String vxlanPassWord;

    private String modelSystem;

    private String modelTag;
    private String esId;
    private boolean operationMonitor;
    private boolean autoManage;
    private boolean logManage;
    private boolean propManage;
    private String templateName;

    //业务分类
    private String modelClassify;

}
