package cn.mw.monitor.engineManage.api.param.engineManage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
public class AddOrUpdateEngineManageParam {
    private String id;

    private String engineName;

    private String proxyName;

    private String serverIp;

    private String mode;

    private String description;

    private String encryption;

    private String keyConsistency;

    private String sharedKey;

    private String publisher;

    private String title;

    private String compress;

    private String performance;

    private String creator;

    private Date createDate;

    private String modifier;

    private Integer monitorHostNumber;

    private Integer monitoringItemsNumber;

    private Date modificationDate;

//    private List<List<Integer>> department;

    private String proxyId;

    private String port;

    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;
    /**
     * 删除标识符
     */
    private Boolean deleteFlag;
    /**
     * 监控服务器id
     */
    private int monitorServerId;
    /**
     * 活动代理地址
     */
    private String proxyAddress;

}
