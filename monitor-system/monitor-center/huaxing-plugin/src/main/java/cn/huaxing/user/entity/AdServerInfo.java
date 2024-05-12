package cn.huaxing.user.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author guiquanwnag
 * @datetime 2023/8/24
 * @Description AD域服务器信息
 */
@Data
public class AdServerInfo implements Serializable {

    private String serverName;

    private String ipAddress;

    private String port;

    private String adAccount;

    private String adPasswd;

}
