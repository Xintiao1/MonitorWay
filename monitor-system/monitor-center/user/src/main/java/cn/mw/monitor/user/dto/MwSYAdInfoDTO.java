package cn.mw.monitor.user.dto;

import lombok.Data;

/**
 * mw_adAuthentic_mapper  映射配置信息
 * @author zy.quaee
 * @date 2021/6/7 14:13
 **/
@Data
public class MwSYAdInfoDTO {

    private String serverName;

    private String ipAddress;

    private String port;

    private String ldapAdminPasswd;

    private String ldapAdminUser;
}
