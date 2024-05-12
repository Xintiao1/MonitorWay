package cn.mw.monitor.scanrule.model;

import lombok.Data;

@Data
public class RuleView {
    private String protoType;
    private String port;
    private String version;
    private String community;
    private String securityName;
    private String contextName;
    private String securityLevel;
    private String authProtocol;
    private String authToken;
    private String privProtocol;
    private String privToken;
}
