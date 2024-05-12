package cn.mw.syslog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginLogDTO {
    // 登录名
    private String userName;
    // Ip地址
    private String userIp;
    //登录时间
    private String createDate;
    //登录是否成功
    private String isSuccess;
    //失败原因
    private String failType;
    //登录方式
    private String loginWay;
}
