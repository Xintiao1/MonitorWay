package cn.mw.monitor.util.entity;

import lombok.Data;

@Data
public class GeneralMessageEntity {
    //
    private String ruleId;

    //企业微信id
    private String id;

    //企业微信密钥
    private String secret;

    //企业应用id
    private String agentId;
}
