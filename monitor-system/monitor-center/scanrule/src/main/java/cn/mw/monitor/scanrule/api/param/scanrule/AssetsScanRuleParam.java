package cn.mw.monitor.scanrule.api.param.scanrule;

import lombok.Data;

@Data
public class AssetsScanRuleParam {
    private String contextName;
    private String keyValue;
    private String port;
    private String privProtocol;
    private String privToken;
    private String protoType;
    private String securityLevel;
    private String securityName;
    private String snmpOID;
    private String community;
    private String authProtocol;
    private String authToken;
    private String version;
}
