package cn.mw.monitor.service.alert.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/8/13 16:52
 */
@Data
public class WeLinkRuleParam {
    private String ruleId;
    private String appId;
    private String appSecret;
    private String publicAccId;
}
