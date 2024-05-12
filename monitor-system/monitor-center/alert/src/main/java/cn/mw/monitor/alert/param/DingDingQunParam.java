package cn.mw.monitor.alert.param;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author lbq
 * @date 2021/4/14 14:16
 */
@Data
public class DingDingQunParam {
    private String ruleId;
    @Size(max=128,message = "最大长度不能超过128")
    private String webHook;
    @Size(max=128,message = "最大长度不能超过128")
    private String keyWord;
    @Size(max=128,message = "最大长度不能超过128")
    private String secret;
}
