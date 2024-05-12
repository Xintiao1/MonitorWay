package cn.mw.monitor.weixin.entity;

import lombok.Data;

@Data
public class WeixinFromEntity {
    //
    private String ruleId;

    //微信密钥
    private String secret;

    //应用id
    private String agentId;

    private String alertTempleate;

    private String recoveryTempleate;
}
