package cn.mw.monitor.model.param;

import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2020/10/30 11:53
 * @Version 1.0
 */
@Data
public class MwModelTPServerTable {

    private int id;

    private String monitoringServerName;

    private String monitoringServerIp;

    private String monitoringServerUrl;

    private String monitoringServerUser;

    private String monitoringServerPassword;

    private String monitoringServerVersion;

    private String monitoringServerType;
    /**
     * 是否是最主要监控服务器（只能有一个主）
     */
    private Boolean mainServer;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;
    /**
     * 删除标识符
     */
    private Boolean deleteFlag;

    /**
     * 密码加密标识
     */
    private Boolean encryptedFlag;

}
