package cn.mw.monitor.service.assets.param;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/5/22 15:24
 * @Version 1.0
 */
@Data
@ToString
@ApiModel(value = "新增或修改带外资产数据")
public class AddUpdateOutbandAssetsParam {

    /**
     * 主键ID
     */
    @ApiModelProperty(value = "ID")
    private String id;

    /**
     * zabbix主机分组id
     */
    @ApiModelProperty(value = "groupId")
    private String hostGroupId;

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

//    /**
//     * 协议类型
//     */
//    @ApiModelProperty(value = "协议类型")
//    private String version;

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

//    /**
//     * 资产分组名
//     */
//    @ApiModelProperty(value = "资产分组名")
//    private String groupTypeName;

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
     * 监控方式
     */
    @ApiModelProperty(value = "监控方式(1.AGENT 2.SNMP 3.JMX 4.ICMP 5.IMPI)")
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
    @ApiModelProperty(value = "启动监控状态")
    private Boolean monitorFlag;

    /**
     * 启动配置状态
     */
    @ApiModelProperty(value = "启动配置状态")
    private Boolean settingFlag;

    @ApiModelProperty(value = "创建人")
    private String creator;

    @ApiModelProperty(value = "创建时间")
    private Date createDate;

    @ApiModelProperty(value = "修改人")
    private String modifier;

    @ApiModelProperty(value = "修改时间")
    private Date modificationDate;

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;

    @ApiModelProperty(value = "IPMI列表")
    private MwIPMIAssetsDTO mwIPMIAssetsDTO;

    @ApiModelProperty(value = "标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    /**
     * 只修改启动监控状态时一个状态为3
     * 修改为1
     */
    @ApiModelProperty(value = "修改启动监控状态标记")
    private Integer flag = 1;

    /**
     * 第三方监控服务器id
     */
    @ApiModelProperty(value = "第三方监控服务器id")
    private Integer monitorServerId;
    /**
     * 批量删除的主键数组
     */
    @ApiModelProperty(value = "批量删除的主键数组")
    private List<String> ids;

    /**
     * 带内IP
     */
    @ApiModelProperty(value = "带内IP用于创建snmp资产")
    private String inBandIp;

    //扫描成功表id
    @ApiModelProperty(value = "扫描成功表id")
    private Integer scanSuccessId;

    //扫描成功表id
    @ApiModelProperty(value = "是否本机")
    private String pollingMode;


    /**
     * 启动立即执行
     */
    @ApiModelProperty(value = "启动立即执行")
    private boolean checkNowFlag;

    @ApiModelProperty(value = "编辑前IP")
    private String editBeforeIp;

    @ApiModelProperty(value = "是否需要批量修改责任人")
    private boolean principalcheckbox;
    @ApiModelProperty(value = "是否需要批量修改机构")
    private boolean orgIdscheckbox;
    @ApiModelProperty(value = "是否需要批量修改用户组")
    private boolean groupIdscheckbox;
    @ApiModelProperty(value = "是否需要批量修改标签列表")
    private boolean outBandAssetsLabelcheckbox;
    @ApiModelProperty(value = "是否需要批量修改监控状态")
    private boolean monitorFlagcheckbox;
    @ApiModelProperty(value = "是否需要批量修改描述")
    private boolean descriptioncheckbox;
    @ApiModelProperty(value = "是否需要批量修改轮询引擎")
    private boolean pollingEnginecheckbox;
    @ApiModelProperty(value = "key值是监控服务器id, value值是轮询引擎对应pollingEngineId")
    private Map<Integer, String> pollingEngineList;
    @ApiModelProperty(value = "key值是监控服务器id, value值是监控服务器对应的代理id数组")
    private Map<Integer, List<String>> proxyIdList;

}
