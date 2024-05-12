package cn.mw.xiangtai.plugin.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 威胁事件列表
 */
@Data
public class ThreatEventDTO {

    // 系统名
    private String systemName;

    // 日志类型，默认为1
    private Integer logType;

    // 源IP
    private String srcIp;

    // 目的IP
    private String dstIp;

    // 告警类型
    private String alertType;

    // 告警名称
    private String alertName;

    // 告警等级 (1 - 高, 2 - 中, 3 - 低)
    private Integer alertLevel;

    // 攻击是否成功 (1 - 成功, 2 - 疑似成功, 3 - 失败)
    private String alertStat;

    // 告警设备IP
    private String alertDevIp;

    // 国家
    private String country;

    // 日志自己的时间戳
    private LocalDateTime timestamp;

    // 日志入库的创建时间，默认为当前时间
    private LocalDateTime createTime;
}
