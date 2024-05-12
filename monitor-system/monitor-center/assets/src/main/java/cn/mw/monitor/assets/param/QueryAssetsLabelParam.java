package cn.mw.monitor.assets.param;

import cn.mw.monitor.service.label.param.LogicalQueryLabelParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author syt
 * @Date 2021/7/22 18:09
 * @Version 1.0
 */
@Data
public class QueryAssetsLabelParam {
    private String fuzzyQuery;//模糊查询
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



    @ApiModelProperty(value = "带外IP")
    private String ipAddress;
    @ApiModelProperty(value = "有形资产IP")
    private String inBandIp;
    @ApiModelProperty(value = "有形资产带外IP")
    private String outBandIp;


    /**
     * 资产类型
     */
    @ApiModelProperty(value = "资产类型")
    private Integer assetsTypeId;
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


    //逻辑标签查询条件
    private List<List<LogicalQueryLabelParam>> logicalQueryLabelParamList;





    @ApiModelProperty(value = "用户Id")
    private Integer userId;

    @ApiModelProperty(value = "群Id列表")
    private List<Integer> groupIds;

    private List<Integer> orgIds;

    @ApiModelProperty(value = "是否是admin")
    private Boolean isAdmin;


    @ApiModelProperty(value = "资产编号")
    private String assetsNumber;


    @ApiModelProperty(value = "资产子类型")
    private Integer subAssetsTypeId;

    /**
     * 资产内容
     */
    @ApiModelProperty(value = "资产内容")
    private String assetsContent;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remarks;

    private String perm;

    private String tableName;

    private String moduleType;

    private int tableType = 1;

    private List<String> assetsIds;

}
