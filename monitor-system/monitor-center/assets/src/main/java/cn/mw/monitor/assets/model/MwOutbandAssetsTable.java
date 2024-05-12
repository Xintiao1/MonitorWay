package cn.mw.monitor.assets.model;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.model.OrgDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/5/21 15:57
 * @Version 1.0
 */
@Data
public class MwOutbandAssetsTable {

    private String id;
    /**
     * ip地址
     */
    private String ipAddress;
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
    /**
     * 资产类型
     */
    private Integer assetsTypeId;
    /**
     * 资产子类型
     */
    private Integer assetsTypeSubId;
    /**
     * 轮训引擎
     */
    private String pollingEngine;
    /**
     * 监控方式（IPMI）
     */
    private Integer monitorMode;
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
     * 创建人
     */
    private String creator;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 修改人
     */
    private String modifier;
    /**
     * 修改时间
     */
    private Date modificationDate;
    /**
     * 第三方监控服务器id
     */
    private Integer monitorServerId;
    /**
     * 第三方监控服务器名称
     */
    private String monitorServerName;

    private String vendorSmallIcon;

    private Integer vendorCustomFlag;

    private Integer scanSuccessId;

    private String templateId;

    @ApiModelProperty(value="轮询引擎的状态")
    private String pollingMode;

    /**
     * 资产类型
     */
    @ApiModelProperty(value = "资产类型")
    private String assetsTypeName;

    /**
     * 资产子类型
     */
    @ApiModelProperty(value = "资产子类型")
    private String assetsTypeSubName;

    /**
     * 轮询引擎
     */
    @ApiModelProperty(value = "轮询引擎")
    private String pollingEngineName;

    /**
     * 监控方式
     */
    @ApiModelProperty(value = "监控方式")
    private String monitorModeName;

    @ApiModelProperty(value = "资产状态")
    private String itemAssetsStatus;

    @ApiModelProperty(value = "有形资产名称")
    private String tangibleAssetsName;

    @ApiModelProperty(value = "有形资产信息")
    private MwTangibleassetsTable tangibleassetsTable;

    @ApiModelProperty(value = "带外资产监控主机名称")
    private String tpServerHostName;

    @ApiModelProperty(value = "轮询引擎取值回显")
    private Map<String,String> pollingEngines;

    private List<OrgDTO> department;

    private List<GroupDTO> group;
}
