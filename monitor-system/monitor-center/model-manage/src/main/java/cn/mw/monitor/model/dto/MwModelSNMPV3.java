package cn.mw.monitor.model.dto;

import lombok.Data;

@Data
public class MwModelSNMPV3 {
    private String port;
    //安全名称
    private String securityname;
    //安全级别
    private String securitylevel;
    //验证口令
    private String authpassphrase;
    //私钥
    private String privpassphrase;
    //验证协议
    private String authprotocol;
    //隐私协议
    private String privprotocol;
    //上下文
    private String contextname;

    private String assetsId;

}