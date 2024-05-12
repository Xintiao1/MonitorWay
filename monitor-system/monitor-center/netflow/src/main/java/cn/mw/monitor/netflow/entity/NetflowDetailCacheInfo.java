package cn.mw.monitor.netflow.entity;

import cn.mw.monitor.netflow.param.TimeParam;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className NetflowDetailCacheInfo
 * @description 流量明细缓存信息
 * @date 2023/4/12
 */
@Data
public class NetflowDetailCacheInfo {

    /**
     * kibana请求参数
     */
    private String kibanaInfo;

    /**
     * 开始时间参数
     */
    private TimeParam startTime;

    /**
     * 结束时间参数
     */
    private TimeParam endTime;

    /**
     * 已选择的栏目
     */
    private List<String> selectedColumns;

}
