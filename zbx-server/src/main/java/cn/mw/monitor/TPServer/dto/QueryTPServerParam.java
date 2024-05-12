package cn.mw.monitor.TPServer.dto;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author syt
 * @Date 2020/10/30 15:26
 * @Version 1.0
 */
@Data
public class QueryTPServerParam extends BaseParam {

    private int id;

    private String monitoringServerName;

    private String monitoringServerIp;

    private String monitoringServerUrl;

    private String monitoringServerUser;

    private String monitoringServerPassword;

    private String monitoringServerVersion;

    private String monitoringServerType;

    private Boolean mainServer;

    private String creator;

    private String modifier;

    private Date createDateStart;

    private Date createDateEnd;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private String prem;

    private Integer userId;

    private List<Integer> groupIds;

    private List<Integer> orgIds;

    private Boolean isAdmin;

    private String fuzzyQuery;
}
