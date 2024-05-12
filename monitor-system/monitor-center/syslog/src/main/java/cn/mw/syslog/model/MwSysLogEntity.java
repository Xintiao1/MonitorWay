package cn.mw.syslog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 系统日志实例
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MwSysLogEntity {

    // 日志id
    private Integer logId;
    // 方法备注
    private String remark;
    // 调用用户名称
    private String userName;
    // 调用用户Ip
    private String userIp;
    // 调用类名
    private String className;
    // 调用方法名
    private String mothodName;
    // 调用方法耗时
    private Long exeuTime;
    // 日志类型：0.系统日志 1.登录日志 2.执行日志
    private Integer type;
    // 方法调用时间
    private String createDate;
    //登录是否成功
    private String isSuccess;
    //失败原因
    private String failType;
    //登录方式
    private String loginWay;

}