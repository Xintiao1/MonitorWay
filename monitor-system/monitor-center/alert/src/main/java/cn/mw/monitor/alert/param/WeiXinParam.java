package cn.mw.monitor.alert.param;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

/**
 * @author xhy
 * @date 2020/8/13 16:33
 */
@Data
public class WeiXinParam {
    private String ruleId;
    @Size(max=128,message = "最大长度不能超过128")
    private String agentId;
    @Size(max=128,message = "最大长度不能超过128")
    private String appSecret;
    @Size(max=128,message = "最大长度不能超过128")
    private String token;
    @Size(max=128,message = "最大长度不能超过128")
    private String alertTempleate;
    @Size(max=128,message = "最大长度不能超过128")
    private String recoveryTempleate;
}
