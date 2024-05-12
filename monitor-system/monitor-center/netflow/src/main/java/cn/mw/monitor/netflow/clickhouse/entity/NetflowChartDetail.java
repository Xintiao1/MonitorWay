package cn.mw.monitor.netflow.clickhouse.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author guiquanwnag
 * @datetime 2023/8/1
 * @Description 流量管理的折线图详细数据
 */
@Data
public class NetflowChartDetail  implements Serializable {


    /**
     * 时间（X轴）
     */
    private String dateTime;

    /**
     * 值（Y轴）
     */
    private double value;

    /**
     * 值的单位
     */
    private String unitByReal;

    /**
     * 时间间隔的值
     */
    private int timeInterval;

}
