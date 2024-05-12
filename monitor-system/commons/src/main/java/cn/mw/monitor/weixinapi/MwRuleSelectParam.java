package cn.mw.monitor.weixinapi;

import lombok.Data;
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/27 14:35
 */
@Data
public class MwRuleSelectParam {
    private String key;//标识
    private Integer deep;//层级
    private String condition;//相互关系，并或
    private String name;//类型名称：资产，资产类型，指标，标签，数据来源
    private String relation; //包含，不包含，正则，大于，小于
    private String value;//输入的值
    private String parentKey;//父节点标识
    List<MwRuleSelectParam> constituentElements;//子关系
    private String uuid;
}
