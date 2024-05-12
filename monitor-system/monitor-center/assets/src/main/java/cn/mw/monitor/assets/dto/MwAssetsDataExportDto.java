package cn.mw.monitor.assets.dto;

import cn.mw.monitor.service.assets.model.MwAllLabelDTO;
import cn.mw.monitor.service.assets.param.QueryTangAssetsLabelParam;
import cn.mw.monitor.service.label.param.LogicalQueryLabelParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author qzg
 * @date 2021/6/24
 */
@Data
public class MwAssetsDataExportDto {
    private String id;

    private String labelName;

    private String assetsId;

    private String inBandIp;
    /**
     * 资产名称
     */
    private String assetsName;

    /**
     * 主机名称
     */
    private String hostName;
    /**
     * 带外IP
     */
    private String outBandIp;

    /**
     * 资产类型
     */
    private Integer assetsTypeId;
    private String assetsTypeName;

    /**
     * 资产子类型
     */
    private Integer assetsTypeSubId;
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

    private String modifier;

    private String creator;

    private Date createDateStart;

    private Date createDateEnd;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private List<QueryTangAssetsLabelParam> labelList;

    private List<MwAllLabelDTO> allLabelList;

    /**
     * 是否高级查询
     */
    private Boolean isSelectLabel = false;

    private String prem;

    private Integer userId;

    private List<Integer> groupIds;

    private List<Integer> orgIds;
    //逻辑标签查询条件
    private List<List<LogicalQueryLabelParam>> logicalQueryLabelParamList;
    //标签查询后的资产id
    private List<String> assetsIds;

    private int monitorServerId;

    private Boolean isAdmin;
    @ApiModelProperty(value = "当其为1时，查询所有关联zabbix的资产")
    private int zabbixFlag;

    private List<String> header;

    private List<String> headerName;

    private boolean isQueryAssetsStatus;

}
