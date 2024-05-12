package cn.mw.module.security.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/9/9 17:29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddEsLogParam {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("事件名称")
    private String eventName;
    @ApiModelProperty("运维对象")
    private String orgName;
    @ApiModelProperty("上报单位")
    private String reportOrg;
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
    @ApiModelProperty("攻击类型")
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
    @ApiModelProperty("处理状态")
    private String disposalStatus;

    @ApiModelProperty("來源类型")
    private String type;

    private String message;
    private String destDomain;
    private String isRecord;
    private String isTest;
    private String disposalType;
}
