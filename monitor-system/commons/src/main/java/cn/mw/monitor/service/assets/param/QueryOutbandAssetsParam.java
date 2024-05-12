package cn.mw.monitor.service.assets.param;

import cn.mw.monitor.service.assets.model.MwAllLabelDTO;
import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsLabelParam;
import cn.mw.monitor.service.label.param.LogicalQueryLabelParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;
import java.util.List;

/**
 * @author syt
 * @Date 2020/6/22 14:45
 * @Version 1.0
 */
@Data
@Builder
@ApiModel(value = "查询外带资产数据")
public class QueryOutbandAssetsParam extends BaseParam {
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "标签名称")
    private String labelName;
    @ApiModelProperty(value = "zabbix主机id")
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
     * 带外IP
     */
    @ApiModelProperty(value = "带外IP")
    private String ipAddress;

    /**
     * 资产类型
     */
    @ApiModelProperty(value = "资产类型")
    private Integer assetsTypeId;

    /**
     * 资产子类型
     */
    @ApiModelProperty(value = "资产子类型")
    private Integer assetsTypeSubId;

    /**
     * 轮训引擎
     */
    @ApiModelProperty(value = "轮询引擎id")
    private String pollingEngine;

    /**
     * 监控方式
     */
    @ApiModelProperty(value = "监控方式")
    private Integer monitorMode;

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
    @ApiModelProperty(value = "删除标识符")
    private Boolean deleteFlag;

    /**
     * 启动监控状态
     */
    @ApiModelProperty(value = "监控状态")
    private Boolean monitorFlag;

    /**
     * 启动配置状态
     */
    @ApiModelProperty(value = "配置状态")
    private Boolean settingFlag;
    @ApiModelProperty(value = "修改人")
    private String modifier;
    @ApiModelProperty(value = "创建人")
    private String creator;

    @ApiModelProperty(value = "创建时间查询开始")
    private Date createDateStart;
    @ApiModelProperty(value = "创建时间查询结束")
    private Date createDateEnd;
    @ApiModelProperty(value = "修改时间查询开始")
    private Date modificationDateStart;
    @ApiModelProperty(value = "修改时间查询结束")
    private Date modificationDateEnd;
    @ApiModelProperty(value = "标签数组")
    private List<QueryTangAssetsLabelParam> labelList;

    @ApiModelProperty(value = "MwAllLabelDTO列表")
    private List<MwAllLabelDTO> allLabelList;

    /**
     * 是否高级查询
     */
    @ApiModelProperty(value = "是否高级查询")
    private Boolean isSelectLabel = false;

    private String prem;

    @ApiModelProperty(value = "用户Id")
    private Integer userId;

    @ApiModelProperty(value = "群Id列表")
    private List<Integer> groupIds;

    private List<Integer> orgIds;

    @ApiModelProperty(value = "是否是admin")
    private Boolean isAdmin;

    //逻辑标签查询条件
    @ApiModelProperty(value = "逻辑标签查询条件")
    private List<List<LogicalQueryLabelParam>> logicalQueryLabelParamList;
    //标签查询后的资产id
    @ApiModelProperty(value = "标签查询后的资产id")
    private List<String> ids;

    @ApiModelProperty(value = "关联监控服务器id")
    private Integer monitorServerId;

    @ApiModelProperty(value = "是否是首页查询")
    private int isHomePageType;

    @ApiModelProperty(value = "模糊查询条件")
    private String fuzzyQuery;

    @Tolerate
    QueryOutbandAssetsParam (){}
}
