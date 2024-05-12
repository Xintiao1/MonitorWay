package cn.mw.monitor.weixin.entity;

import lombok.Data;
import javax.validation.constraints.Size;

@Data
public class AliyunSmsFromEntity{

    private String ruleId;
    @Size(max=128,message = "最大长度不能超过128")
    private String signName;
    @Size(max=128,message = "最大长度不能超过128")
    private String templateCode;
    @Size(max=128,message = "最大长度不能超过128")
    private String accessKeyId;
    @Size(max=128,message = "最大长度不能超过128")
    private String accessKeySecret;
}
