package cn.mw.monitor.engineManage.api.param.engineManage;


import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
public class QueryEngineManageParam extends BaseParam {

    private String id;

    private String engineName;

    private String serverIp;

    private String mode;

    private String description;

    private String encryption;

    private String keyConsistency;

    private String sharedKey;

    private String publisher;

    private String title;

    private String compress;

    private Integer monitorHostNumber;

    private Integer monitoringItemsNumber;

    private String performance;

    private String creator;

    private String modifier;

    private Date createDateStart;

    private Date createDateEnd;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    private String port;

    private String proxyId;

    private String prem;

    private Integer userId;

    private List<Integer> groupIds;

    private List<Integer> orgIds;

    private Boolean isAdmin;

    private int monitorServerId;

    private String fuzzyQuery;
}
