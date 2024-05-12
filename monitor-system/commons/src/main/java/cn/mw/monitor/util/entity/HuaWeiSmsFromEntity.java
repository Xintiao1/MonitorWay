package cn.mw.monitor.util.entity;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class HuaWeiSmsFromEntity {

    private String ruleId;
    @Size(max=128,message = "最大长度不能超过128")
    private String signName;
    @Size(max=128,message = "最大长度不能超过128")
    private String appKey;
    @Size(max=128,message = "最大长度不能超过128")
    private String appSecret;
    @Size(max=128,message = "最大长度不能超过128")
    private String sender;//短信签名通道号
    @Size(max=128,message = "最大长度不能超过128")
    private String templateId;
    @Size(max=128,message = "最大长度不能超过128")
    private String recoveryTemplateId;//恢复告警模板
}
