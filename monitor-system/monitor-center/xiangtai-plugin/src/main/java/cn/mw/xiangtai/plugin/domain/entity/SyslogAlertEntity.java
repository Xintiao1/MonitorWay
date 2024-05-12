package cn.mw.xiangtai.plugin.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("SYSLOG_ALERT")
public class SyslogAlertEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField(value = "system_name")
    // 系统名
    private String systemName;

    @TableField(value = "log_type")
    // 日志类型，默认为1
    private Integer logType = 1;

    @TableField(value = "src_ip")
    // 源IP
    private String srcIp;

    @TableField(value = "src_port")
    // 源端口
    private String srcPort;

    @TableField(value = "dst_ip")
    // 目的IP
    private String dstIp;

    // 目的设备
    @TableField(exist = false)
    private String dstDevice;

    @TableField(value = "dst_port")
    // 目的端口
    private String dstPort;

    @TableField(value = "proto")
    // 传输层协议
    private String proto;

    @TableField(value = "appproto")
    // 应用层协议
    private String appProto;

    @TableField(value = "alert_type")
    // 告警类型
    private String alertType;

    @TableField(value = "alert_name")
    // 告警名称
    private String alertName;

    @TableField(value = "alert_level")
    // 告警等级 (1 - 高, 2 - 中, 3 - 低)
    private Integer alertLevel;

    @TableField(value = "timestamp")
    // 标准时间格式
    private LocalDateTime timestamp;

    @TableField(value = "alert_stat")
    // 攻击是否成功 (1 - 成功, 2 - 疑似成功, 3 - 失败)
    private String alertStat;

    @TableField(value = "attack_srcadd")
    // 攻击路径溯源地址
    private String attackSrcAdd;

    @TableField(value = "alert_devip")
    // 告警设备IP
    private String alertDevIp;

    @TableField(value = "srcpack_add")
    // 攻击原包映射地址
    private String srcPackAdd;

    @TableField(value = "url")
    // 协议详情
    private String url;

    @TableField(value = "http_method")
    // HTTP请求方式
    private String httpMethod;

    @TableField(value = "http_request_headers")
    // 请求头信息
    private String httpRequestHeaders;

    @TableField(value = "http_response_headers")
    // 返回头信息
    private String httpResponseHeaders;

    @TableField(value = "http_request_content")
    // 请求内容
    private String httpRequestContent;

    @TableField(value = "http_response_content")
    // 返回内容
    private String httpResponseContent;

    @TableField(value = "cmid")
    // 告警组合规则ID
    private String cmid;

    @TableField(value = "msg")
    // 规则说明
    private String msg;

    @TableField(value = "hostname")
    // 域名
    private String hostname;

    @TableField(value = "refer")
    // Referer
    private String refer;

    @TableField(value = "user_agent")
    // User-Agent信息
    private String userAgent;

    @TableField(value = "status")
    // HTTP返回码
    private String status;

    @TableField(value = "xff")
    // 负载均衡IP
    private String xff;

    @TableField(value = "country")
    // 国家
    private String country;

    @TableField(value = "create_time")
    // 创建时间，默认为当前时间
    private LocalDateTime createTime;

    @TableField(exist = false)
    //目标IP对应信息
    private XiangtaiIPMSEntity ipmsInfo;

}
