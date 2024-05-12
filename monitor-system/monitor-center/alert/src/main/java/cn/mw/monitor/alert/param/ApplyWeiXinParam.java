package cn.mw.monitor.alert.param;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

/**
 * @author xhy
 * @date 2020/11/3 16:15
 */
@Data
public class ApplyWeiXinParam {
    private String ruleId;
    @Size(max=128,message = "最大长度不能超过128")
    private String applyId;
    @Size(max=128,message = "最大长度不能超过128")
    private String agentId;
    @Size(max=128,message = "最大长度不能超过128")
    private String secret;
}
