package cn.mw.monitor.model.param;

import cn.mw.monitor.service.scan.model.SecurityLevel;
import cn.mw.monitor.service.scan.model.SecurityProtocolType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MwModelScanResultSuccessParam {
    private Integer id;
    private Integer scanruleId;
    private String templateId;
    private String templateName;
    private String hostGroupId;
    private String scanBatch;
    private String hostName;
    private String inBandIp;
    private String sysObjId;
    private String manufacturer;
    private String description;
    private String specifications;
    private Integer assetsTypeId;
    private Integer assetsTypeSubId;
    //模型分组、模型id
    private List modelGroup;
    private String groupTypeName;
    private String pollingEngine;
    private String monitorMode;
    private String monitorModeVal;
    private Date scanTime;
    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;
    private String port;
    private String monitorPort;
    private String community;
    private String securityName;
    private String contextName;
    private SecurityLevel securityLevel;
    private SecurityProtocolType authProtocol;
    private String authToken;
    private SecurityProtocolType privProtocol;
    private String privToken;
    private String modelIndex;

    //通过调用zabbix api返回的结果设置
    private String zabbixHostId;

    //模版匹配表id
    private int templateMatchId;

    //资产表对应的扫描成功表id
    private int scanSuccessId;

    //监控服务器id
    private int monitorServerId;

    //设备idcode
    private String deviceCode;
    //实例列表批量纳管使用
    private String instanceName;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;
}
