package cn.mw.monitor.service.scan.model;

import cn.mw.monitor.util.EnDecryptionUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ScanResultSuccess {
    private Integer id;
    private Integer scanruleId;
    private String templateId;
    private String templateName;
    private String groupId;
    private String scanBatch;
    private String hostName;
    private String ipAddress;
    private String sysObjId;
    private String brand;
    private String description;
    private String specifications;
    private Integer assetsTypeId;
    private Integer assetsSubTypeId;
    private String assetsTypeName;
    private String assetsSubTypeName;
    private String groupTypeName;
    private String pollingEngine;
    private String pollingEngineName;
    private String monitorMode;
    private String monitorModeName;
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
    private int scanSuccessIdInAssets;

    //监控服务器id
    private int monitorServerId;
    //监控服务器Name
    private String monitorServerName;

    //监控服务器id
    private boolean randomName;

    //设备idcode
    private String deviceCode;
    //实例列表批量纳管使用
    private String instanceName;
    @ApiModelProperty(value = "启动监控状态")
    private Boolean monitorFlag;

    @ApiModelProperty("用户组ID列表")
    private List<Integer> groupIds;

    @ApiModelProperty("负责人ID列表")
    private List<Integer> userIds;

    @ApiModelProperty("机构ID列表")
    private List<List<Integer>> orgIds;

    @ApiModelProperty("Snmp版本")
    private Integer snmpVersion;

    public String toSystemInfo(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("templateId:").append(templateId)
                .append(",").append("templateName:").append(templateName)
                .append(",").append("zabbixHostId:").append(zabbixHostId)
                .append(",").append("instanceName:").append(instanceName)
                .append(",").append("templateMatchId:").append(templateMatchId)
                ;
        return stringBuffer.toString();
    }

    public void decrSnmpData(){
        setCommunity(EnDecryptionUtils.entrypt(getCommunity()));
        setSecurityName(EnDecryptionUtils.entrypt(getSecurityName()));
        setContextName(EnDecryptionUtils.entrypt(getContextName()));
        setAuthToken(EnDecryptionUtils.entrypt(getAuthToken()));
        setPrivToken(EnDecryptionUtils.entrypt(getPrivToken()));
    }
}
