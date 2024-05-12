package cn.mw.monitor.netflow.entity;

import lombok.Data;

/**
 * @author guiquanwnag
 * @datetime 2023/7/24
 * @Description
 */
@Data
public class CapDetailChart {

    /**
     * X轴的字段值
     */
    private Integer timeInterval;

    /**
     * Y轴数据，ES标中的总数
     */
    private Integer times;


}
