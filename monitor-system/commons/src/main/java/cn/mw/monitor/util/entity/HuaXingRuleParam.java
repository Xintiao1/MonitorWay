package cn.mw.monitor.util.entity;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/8/13 16:52
 */
@Data
public class HuaXingRuleParam {
    private String ruleId;
    private String appId;
    private String appSecret;
    private String pluginId;
    private String token;
    private String url;
    private String sender;
    private String sessionType;
}
