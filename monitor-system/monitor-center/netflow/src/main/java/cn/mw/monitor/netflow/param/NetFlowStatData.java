package cn.mw.monitor.netflow.param;

import lombok.Data;

/**
 * @author gui.quanwang
 * @className NetFlowStatData
 * @description 流量统计数据
 * @date 2022/8/10
 */
@Data
public class NetFlowStatData {

    /**
     * 时间戳
     */
    private String timeStamp;

    /**
     * 这段时间的统计值
     */
    private double statData;
}
