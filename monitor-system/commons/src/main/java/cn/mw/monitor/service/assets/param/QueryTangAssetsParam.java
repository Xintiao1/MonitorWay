package cn.mw.monitor.service.assets.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.assets.model.MwAllLabelDTO;
import cn.mw.monitor.service.label.param.LogicalQueryLabelParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("查询下拉框带外资产数据")
public class QueryTangAssetsParam  extends BaseParam {

    private String id;

    private String labelName;

    private String assetsId;

    private String inBandIp;

    private String fuzzyQuery;//模糊查询
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
     * 查询资产的条件类型
     */
    private Integer treeType;

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

    private Integer monitorServerId;

    private Boolean isAdmin;
    @ApiModelProperty(value = "当其为1时，查询所有关联zabbix的资产")
    private int zabbixFlag;

    /**
     * 终端排序
     */
    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;

    /**
     * 时间类型
     */
    private Integer dateType;

    @ApiModelProperty("资产类型")
    private String assetsType;

    /**
     * 是否查看接口列表
     */
    private Integer netFlowInterface = 0;

    //是否忽略数据权限控制  true忽略，可在定时任务时设置为true，避免没有userId导致报错
    private Boolean skipDataPermission;

    //是否查询告警字段
    private boolean isAlertQuery;

    private Boolean isQueryAssetsState;

    @ApiModelProperty("模型实例ids")
    private List<Integer> instanceIds;
}
