package cn.mw.module.security.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/9/7 15:14
 */
@Data
public class EslogParam extends BaseParam {
    private String id;
   private String[] queryDate;
   @ApiModelProperty("事件名称")
    private String eventName;
    @ApiModelProperty("运维对象")
    private String orgName;
    @ApiModelProperty("资产IP")
    private String ip;
    @ApiModelProperty("生成时间")
    private String logDate;
    @ApiModelProperty("告警等级")
    private String alertLevel;
    @ApiModelProperty("优先级")
    private String priorityLevel;
    @ApiModelProperty("分类")
    private String operationEventType;
    @ApiModelProperty("类型")
    private String eventType;
    @ApiModelProperty("状态")
    private String code;

    @ApiModelProperty("源IP地址")
    private String sourceIp;
    @ApiModelProperty("源端口")
    private String sourcePort;
    @ApiModelProperty("目标IP地址")
    private String destIp;
    @ApiModelProperty("目标端口")
    private String destPort;
    @ApiModelProperty("攻击次数")
    private String occurCount;
    @ApiModelProperty("攻击结果")
    private String result;

    @ApiModelProperty("来源")
    private String device;
    @ApiModelProperty("处理建议")
    private String disposalSuggestion;
    @ApiModelProperty("处理人")
    private String disposalUser;
}
