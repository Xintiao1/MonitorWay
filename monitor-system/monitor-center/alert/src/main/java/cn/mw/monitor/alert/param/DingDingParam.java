package cn.mw.monitor.alert.param;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

/**
 * @author xhy
 * @date 2020/8/13 16:35
 */
@Data
public class DingDingParam {
    private String ruleId;
    @Size(max=128,message = "最大长度不能超过128")
    private String agentId;
    @Size(max=128,message = "最大长度不能超过128")
    private String appKey;
    @Size(max=128,message = "最大长度不能超过128")
    private String appSecret;
}
