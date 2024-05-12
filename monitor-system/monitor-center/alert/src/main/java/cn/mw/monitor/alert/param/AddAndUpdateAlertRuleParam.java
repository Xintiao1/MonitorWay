package cn.mw.monitor.alert.param;

import cn.mw.monitor.service.alert.dto.WeLinkRuleParam;
import cn.mw.monitor.util.entity.*;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;


/**
 * @author xhy
 * @date 2020/8/13 16:24
 */
@Data
public class AddAndUpdateAlertRuleParam extends AlertRuleTable {
    @Valid
    private EmailParam emailParam;
    @Valid
    private WeiXinParam weiXinParam;
    @Valid
    private ApplyWeiXinParam applyWeiXinParam;
    @Valid
    private DingDingParam dingDingParam;
    @Valid
    private DingDingQunParam dingDingGroupParam;
    @Valid
    private AliyunSmsParam aliyunSmsParam;
    @Valid
    private ShenzhenSmsParam shenzhenSmsparam;
    @Valid
    private SYSLogParam sysLogParam;
    @Valid
    private MwCaiZhengTingSMSParam caiZhengTingSMSParam;
    @Valid
    private SYSLogParam guangZhouBankParam;
    @Valid
    private AliYunYuYinlParam aliYunYuYinParam;
    @Valid
    private TengXunSmsFromEntity tengXunSmsFromEntity;
    @Valid
    private HuaWeiSmsFromEntity huaWeiSmsFromEntity;
    @Valid
    private WeLinkRuleParam weLinkRuleParam;
    @Valid
    private HuaXingRuleParam huaXingRuleParam;
    @Valid
    private HuaXingYuYinRuleParam huaXingYuYinRuleParam;

    private Boolean enable;

}
