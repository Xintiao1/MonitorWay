package cn.mw.monitor.util.entity;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class TengXunSmsFromEntity {

    private String ruleId;
    @Size(max=128,message = "最大长度不能超过128")
    private String signName;
    @Size(max=128,message = "最大长度不能超过128")
    private String secretId;
    @Size(max=128,message = "最大长度不能超过128")
    private String secretKey;
    @Size(max=128,message = "最大长度不能超过128")
    private String templateId;
    @Size(max=128,message = "最大长度不能超过128")
    private String appId;
    @Size(max=128,message = "最大长度不能超过128")
    private String recoveryTemplateId;
}
