package cn.mw.monitor.netflow.service;

import cn.mw.monitor.netflow.entity.NetflowDetailCacheInfo;
import cn.mw.monitor.netflow.param.NetFlowDetailParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author guiquanwnag
 * @datetime 2023/7/19
 * @Description 流量明细服务类(clickhouse)
 */
public interface MwNetflowDetailService {

    /**
     * 存储类型为clickhouse
     */
    int STORAGE_CLICKHOUSE = 2;

    /**
     * 获取流量明细
     * @return
     */
    Reply getNetFlowDetail(NetFlowDetailParam param);

    /**
     * 获取流量明细图表
     * @return
     */
    Reply getNetFlowDetailChart(NetFlowDetailParam param);

    /**
     * 获取ES中索引的字段
     * @return
     */
    Reply getNetFlowColumns();




}
