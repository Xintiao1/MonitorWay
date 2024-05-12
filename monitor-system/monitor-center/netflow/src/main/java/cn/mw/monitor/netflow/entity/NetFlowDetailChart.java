package cn.mw.monitor.netflow.entity;

import lombok.Data;

/**
 * @author gui.quanwang
 * @className NetFlowDetailChart
 * @description 流量详情——表格数据
 * @date 2023/3/15
 */
@Data
public class NetFlowDetailChart {

    /**
     * X轴的字段值
     */
    private String columnName;

    /**
     * Y轴数据，ES标中的总数
     */
    private int count;

}
