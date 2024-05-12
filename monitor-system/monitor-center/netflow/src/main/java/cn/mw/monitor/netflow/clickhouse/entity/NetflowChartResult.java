package cn.mw.monitor.netflow.clickhouse.entity;

import cn.mw.monitor.netflow.annotation.ClickHouseColumn;
import lombok.Data;

import java.io.Serializable;

/**
 * @author guiquanwnag
 * @datetime 2023/8/2
 * @Description 交互流量的折线图数据信息
 */
@Data
public class NetflowChartResult  implements Serializable {


    /**
     * sql的时间间隔，不是时间单位，是距离开始时间相差几个时间分割单位
     */
    @ClickHouseColumn
    private Integer timeInterval;

    /**
     * 统计数据
     */
    @ClickHouseColumn
    private Double sumBytes;

    /**
     * 源IP
     */
    @ClickHouseColumn
    private String sourceIp;

    /**
     * 目的IP
     */
    @ClickHouseColumn
    private String dstIp;

}
