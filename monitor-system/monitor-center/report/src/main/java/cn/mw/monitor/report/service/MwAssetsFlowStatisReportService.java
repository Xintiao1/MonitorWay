package cn.mw.monitor.report.service;

import cn.mw.monitor.service.netflow.param.NetflowSearchParam;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletResponse;

/**
 * @author gengjb
 * @description 资产流量统计报表
 * @date 2023/8/28 15:30
 */
public interface MwAssetsFlowStatisReportService {

    /**
     * 获取资产流量信息
     * @return
     */
    Reply getAssetsFlowInfo(NetflowSearchParam netflowSearchParam);

    /**
     * 导出资产流量信息
     * @param netflowSearchParam
     * @return
     */
    void exportAssetsFlowInfo(NetflowSearchParam netflowSearchParam, HttpServletResponse response);
}
