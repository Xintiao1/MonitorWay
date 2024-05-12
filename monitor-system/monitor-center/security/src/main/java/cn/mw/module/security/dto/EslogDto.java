package cn.mw.module.security.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2020/9/7 16:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EslogDto {
    @ApiModelProperty("id")
    private String id;
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
    @ApiModelProperty("处理状态")
    private String disposalStatus;


    @ApiModelProperty("没有解析的消息")
    private String message;
    private String priority;
    private String severity;
    private String severityLabel;
    private String facility;
    private String attackDir;
    private String disposalType;
    private String timestamp;
    private String type;
    private String facilityLabel;
    private String host;
}
