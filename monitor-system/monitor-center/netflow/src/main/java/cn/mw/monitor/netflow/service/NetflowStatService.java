package cn.mw.monitor.netflow.service;

import cn.mw.monitor.netflow.param.NetFlowRequestParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author guiquanwnag
 * @datetime 2023/7/31
 * @Description 流量分析新版（数据源为流量明细数据，数据库为clickhouse）
 */
public interface NetflowStatService {

    Integer DATABASE_CLICKHOUSE = 2;

    /**
     * 未知应用ID
     */
    Integer UNKNOWN_APP_ID = 0;

    /**
     * 未知应用名称
     */
    String UNKNOWN_APP_NAME = "未知应用";

    /**
     * 获取流量监控结果
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply browseResult(NetFlowRequestParam requestParam);

}
