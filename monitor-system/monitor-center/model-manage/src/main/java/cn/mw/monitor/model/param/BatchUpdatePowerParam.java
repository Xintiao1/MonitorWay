package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BatchUpdatePowerParam extends BatchUpdatePowerParentParam{
    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "启动配置状态")
    private Boolean monitorFlag;
    /**
     * 启动立即执行
     */
    @ApiModelProperty(value = "启动立即执行")
    private Boolean checkNowFlag;

    @ApiModelProperty(value = "是否关键设备")
    private Boolean isKeyDevices;

    private Boolean operationMonitor;
    private Boolean autoManage;
    private Boolean logManage;
    private Boolean propManage;

    private String proxyId;//代服务Id
    private String engineId;//轮询引擎Id
    private String pollingEngine;//轮询引擎Id
    private String pollingEngineName;//轮询引擎Id

    //告警使用
    private String modelArea; //区域
    private String modelSystem;//应用系统
    private String modelClassify;//应用分类
    private String modelType;//应用类型
    private String modelTag;//标签
    //使用JSON传递动态字段和值
    private Object classIfyMap;
}
