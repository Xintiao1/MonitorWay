package cn.mw.monitor.logManage.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ParsingRuleSearchParam extends BaseParam {
    private String name;

    private String type;

    private String fuzzyQuery;

}
