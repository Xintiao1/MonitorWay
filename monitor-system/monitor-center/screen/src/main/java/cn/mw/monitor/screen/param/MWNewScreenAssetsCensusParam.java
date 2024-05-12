package cn.mw.monitor.screen.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName MWNewScreenAssetsCensusParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/11/29 14:19
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenAssetsCensusParam  extends BaseParam {

    /**
     * 时间类型 1:昨天  2:今天 3:本周 4:本月
     */
    private int dateType;

    //自定义开始时间
    private String startTime;

    //自定义结束时间
    private String endTime;

    /**
     * 告警级别
     * 1:全部  2:信息  3:警告 4：严重
     */
    private List<String> alertLevel;

    //告警名称
    private String alertName;
}
