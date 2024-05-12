package cn.mw.monitor.weixin.entity;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class DingdingqunFromEntity {
    private String ruleId;
    @Size(max=128,message = "最大长度不能超过128")
    private String webHook;
    @Size(max=128,message = "最大长度不能超过128")
    private String keyWord;
    @Size(max=128,message = "最大长度不能超过128")
    private String secret;

}
