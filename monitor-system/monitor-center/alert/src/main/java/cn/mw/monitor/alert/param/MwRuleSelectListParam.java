package cn.mw.monitor.alert.param;

import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/8/27 14:35
 */
@Data
public class MwRuleSelectListParam extends AddAndUpdateAlertActionDto {
    List<MwRuleSelectParam> mwRuleSelectListParam;
}
