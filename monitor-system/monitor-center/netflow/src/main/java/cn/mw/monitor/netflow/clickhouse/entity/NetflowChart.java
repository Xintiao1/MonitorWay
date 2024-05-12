package cn.mw.monitor.netflow.clickhouse.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guiquanwnag
 * @datetime 2023/8/1
 * @Description 流量管理的折线图图形数据
 */
@Data
public class NetflowChart implements Serializable {


    /**
     * 标题名称
     */
    private String titleName;

    /**
     * 单位
     */
    private String unit;

    /**
     * 折线图详细数据
     */
    private List<NetflowChartDetail> realData;

    private List<NetflowChartDetail> avgData;

    private List<NetflowChartDetail> maxData;

    private List<NetflowChartDetail> minData;

    public NetflowChart() {
        this.realData = new ArrayList<>();
        this.avgData = new ArrayList<>();
        this.maxData = new ArrayList<>();
        this.minData = new ArrayList<>();
    }

}
