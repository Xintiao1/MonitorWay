package cn.mw.monitor.logManage.dto;

import lombok.Data;

@Data
public class DataAggregate {

    private String srcField;

    // private AggregateCycle aggregateCycle;

    /**
     * 时间单位：second、minute、hour、day、week、month
     */
    private String timeUnit;

    /**
     * 时间值
     */
    private Integer value;

}
