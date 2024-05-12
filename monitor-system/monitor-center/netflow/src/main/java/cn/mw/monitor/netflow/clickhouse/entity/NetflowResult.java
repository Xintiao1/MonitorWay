package cn.mw.monitor.netflow.clickhouse.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author guiquanwnag
 * @datetime 2023/8/1
 * @Description 流量管理返回数据
 */
@Data
public class NetflowResult  implements Serializable {


    /**
     * 交互流量折线图数据
     */
    private List<NetflowChart> netFlowChartList;

    /**
     * 交互流量TOP100
     */
    private List<NetFlowTopData> netFlowStatList;

    /**
     * 主机流量折线图数据
     */
    private List<NetflowChart> hostChartList;

    /**
     * 主机流量TOP100
     */
    private List<NetFlowTopData> hostStatList;

    /**
     * 应用流量TOP100
     */
    private List<AppTopData> appStatList;

    /**
     * 应用流量折线图数据
     */
    private List<NetflowChart> appChartList;

    /**
     * 查询开始时间
     */
    private String startTime;

    /**
     * 查询结束时间
     */
    private String endTime;

}
