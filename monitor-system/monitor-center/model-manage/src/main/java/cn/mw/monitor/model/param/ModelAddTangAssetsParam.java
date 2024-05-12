package cn.mw.monitor.model.param;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ModelAddTangAssetsParam {

    @ApiModelProperty(value = "扫描成功id")
    private List<Integer> scanSuccessId;

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "责任人")
    private List<Integer> userIds;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;

    @ApiModelProperty(value = "资产状态")
    private String enable;

    @ApiModelProperty(value="标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty(value = "启动监控状态")
    private Boolean monitorFlag;

    @ApiModelProperty(value = "启动配置状态")
    private Boolean settingFlag;

    @ApiModelProperty(value = "关联的轮询引擎")
    private String pollingEngine;

    @ApiModelProperty(value = "zabbixIds")
    private List<Integer> monitorServerIds;

    @ApiModelProperty(value = "是否忽略code校验，默认使用")
    private boolean ignoreCodeCheck;

    //监控服务器id
    private Integer  monitorServerId;

    ///////// 功能模块  /////////////
    //运维监控
    private Boolean operationMonitor;
    //自动化
    private Boolean autoManage;
    //日志管理
    private Boolean logManage;
    //配置管理
    private Boolean propManage;

    /**
     * 启动立即执行
     */
    @ApiModelProperty(value = "启动立即执行")
    private Boolean checkNowFlag;

    /**
     * 是否纳管资产
     */
    @ApiModelProperty(value = "是否纳管资产")
    private Boolean isMonitor;

    private Boolean batchManage;

    private boolean randomName;
}
