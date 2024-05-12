package cn.mw.monitor.link.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/7/22 9:30
 */
@Data
public class LinkDropDownParam extends BaseParam {
    private String linkId;
    private String linkName;

    private String rootAssetsName;
    private String targetAssetsName;

    private String rootIpAddress;
    private String targetIpAddress;
    private String rootPort;
    private String targetPort;

    @ApiModelProperty("扫描方式 （NQA  ICMP）")
    private String scanType;
    @ApiModelProperty("取值端口（源端口 ROOT  目标端口 TARGET）")
    private String valuePort;
    @ApiModelProperty("是否启动链路探测（ACTIVE 启动  DISACTIVE 禁用）")
    private String enable;

    @ApiModelProperty("目标资产的管理ip地址")
    private String linkTargetIp;



    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    private Date createDateStart;

    private Date createDateEnd;

    private Date modificationDateStart;

    private Date modificationDateEnd;


    private List<String> ids;
    private List<Integer> groupIds;
    private Integer userId;
    private List<Integer> orgIds;
    private  Boolean isAdmin;

    private Date labelDateStart;
    private Date labelDateEnd;

    private List<String> linkIds;

    private Boolean isAdvancedQuery;
    private Integer dropKey;
    private String labelValue;
    private Integer inputFormat;
    //大屏线路使用
    private Boolean isFilterQuery;

    private String fuzzyQuery;

    private String parentId;

    //状态 0：异常 1：正常
    private Integer linkStatus;
}
