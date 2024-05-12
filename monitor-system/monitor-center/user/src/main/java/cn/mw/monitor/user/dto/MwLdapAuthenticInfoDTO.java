package cn.mw.monitor.user.dto;

import lombok.Data;

/**
 * Created by zy.quaee on 2021/9/1 14:48.
 **/
@Data
public class MwLdapAuthenticInfoDTO {

    private String serverName;

    private String ipAddress;

    private String port;

    private String adAccount;

    private String adPasswd;

    /**
     * 密码是否可见
     */
    private Boolean passwordVisible;
}
