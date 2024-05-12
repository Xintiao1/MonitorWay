package cn.mw.monitor.assets.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author qzg
 * @date 2020/06/24
 */
@Data
public class MwAssetsExportTable {
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
//    //厂商对应的大图标和小图标
//    private VendorIconDTO vendorIconDTO;
    /**
     * 品牌小图标
     */
    private String vendorSmallIcon;

    private String templateId;

    private String itemAssetsStatus;

    /**
     * 自定义字段
     */
    private Map<String,String> customFieldValue;

    /**
     * 标签
     */
    private String assetsLabel;

    /**
     * 轮询引擎
     */
    @ApiModelProperty(value = "轮询引擎")
    private String pollingEngineName;

    public String getMonitorModeName() {
        if ("ZabbixAgent".equals(monitorModeName)) {
            return "MWAGENT";
        } else {
            return monitorModeName;
        }
    }

    public void setMonitorModeName(String monitorModeName) {
        this.monitorModeName = monitorModeName;
    }
}
