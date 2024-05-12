package cn.mw.monitor.service.model.param;

import cn.mwpaas.common.utils.CollectionUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzg
 * @date 2020/3/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryModelAssetsParam{

    //实例Id
    private Integer modelInstanceId;
    //资产名称
    private String instanceName;

    //老资产名称
    private String assetsName;
    /**
     * zabbix模版id
     */
    @ApiModelProperty(value = "templateId")
    private String templateId;

    /**
     * 资产ID
     */
    @ApiModelProperty(value = "assetsId")
    private String assetsId;

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
     * 带内IP
     */
    @ApiModelProperty(value = "带内IP")
    private String inBandIp;

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
    @ApiModelProperty(value = "轮训引擎")
    private String pollingEngine;

    /**
     * 监控方式// 1 agent    2 snmp    3 port
     */
    @ApiModelProperty(value = "监控方式(1.AGENT 2.SNMP 3.JMX 4.ICMP 5.IPMI)")
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
    @ApiModelProperty(value = "启动监控状态")
    private Boolean monitorFlag;

    /**
     * 启动配置状态
     */
    @ApiModelProperty(value = "启动配置状态")
    private Boolean settingFlag;

    private String creator;

    private DateParam createDate;

    private String modifier;

    private DateParam modificationDate;

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;


    //扫描成功表id
    private Integer scanSuccessId;

    /**
     * 监控服务器id
     */
    private Integer monitorServerId;

    /**
     * 是否打开拓扑连接
     */
    private Boolean openConnect;

    private String vxlanUser;
    private String vxlanPasswd;

    @ApiModelProperty("模型Indexs")
    private List<String> modelIndexs;
    @ApiModelProperty("模型实例ids")
    private List<Integer> instanceIds;

    //指定返回字段
    private List<String> fieldList;
    //指定不返回字段
    private List<String> noFieldList;
    //是否忽略数据权限控制  true忽略，可在定时任务时设置为true，避免没有userId导致报错
    private Boolean skipDataPermission;

    private Boolean isQueryAssetsState;
    //告警字查询
    private boolean isAlertQuery;

    private List<String> assetsIds;

    //业务系统
    private String modelSystem;
    //业务分类
    private String modelClassify;

    public void setAssetsIds(List<String> assetsIds) {
        this.assetsIds = assetsIds;
        if(CollectionUtils.isEmpty(assetsIds)){return;}
        this.instanceIds = new ArrayList<>();
        for (String id : assetsIds) {
            instanceIds.add(Integer.parseInt(id));
        }
    }

    //定时任务及webScoket指定用户
    private Integer userId;

    //模糊查询字段
    private String fuzzyQuery;

    //是否是或查询
    private Boolean isfuzzyQuery = false;

    //是否精准查询
    private boolean filterQuery;

    /**
     * 模糊查询时在ES中需要设置每个模糊查询字段的值
     * @param fuzzyQuery
     */
    public void setFuzzyQuery(String fuzzyQuery) {
        this.fuzzyQuery = fuzzyQuery;
        this.instanceName = fuzzyQuery != null?fuzzyQuery:instanceName;
        this.inBandIp = fuzzyQuery!= null?fuzzyQuery:inBandIp;
        this.hostName = fuzzyQuery!= null?fuzzyQuery:hostName;
        this.manufacturer = fuzzyQuery!= null?fuzzyQuery:manufacturer;
        this.isfuzzyQuery = fuzzyQuery!= null?true:false;
    }
}
