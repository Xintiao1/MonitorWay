package cn.mw.monitor.service.zbx.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;
import java.util.List;


@Data
public class AlertCountParam extends BaseParam {
    //时间类型 0:自定义 1:今天 2:本周 3:本月 4:本年
    private int dateType;

    //自定义开始时间
    private String startTime;

    //自定义结束时间
    private String endTime;

    //告警级别
    private List<String> severity;

    //确认状态
    private String acknowledged;


}
