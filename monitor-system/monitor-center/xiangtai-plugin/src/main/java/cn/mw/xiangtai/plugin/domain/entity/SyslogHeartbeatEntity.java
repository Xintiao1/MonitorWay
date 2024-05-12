package cn.mw.xiangtai.plugin.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("SYSLOG_HEARTBEAT")
public class SyslogHeartbeatEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField(value = "system_name")
    // 系统名
    private String systemName;

    @TableField(value = "log_type")
    // 日志类型
    private Integer logType;

    @TableField(value = "ip")
    // IP地址
    private String ip;

    @TableField(value = "timestamp")
    // 时间戳
    private LocalDateTime timestamp;

    @TableField(value = "version")
    // 版本
    private String version;

    @TableField(value = "create_time")
    // 创建时间，默认为当前时间
    private LocalDateTime createTime;

}
