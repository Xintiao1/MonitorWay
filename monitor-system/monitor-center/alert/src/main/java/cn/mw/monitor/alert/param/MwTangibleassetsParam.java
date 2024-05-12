package cn.mw.monitor.alert.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/03/16
 *
 */
@Data
public class MwTangibleassetsParam {

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

    private String tpServerHostName;
    private String assetsUuid;
    private String assetsSerialnum;
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
}
