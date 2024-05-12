package cn.mw.monitor.report.service;

import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.MwCustomReportParam;

import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName MWRreportExportService
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/12/7 15:21
 * @Version 1.0
 **/
public interface MWRreportExportService {

    void cpuNewsReportAllExport(RunTimeQueryParam param, HttpServletResponse response);

    void diskUseReportAllExport(TrendParam trendParam, HttpServletResponse response);

    void assetsUsabilityReportAllExport(RunTimeQueryParam param, HttpServletResponse response);

    void lineFlowReportAllExport(TrendParam trendParam, HttpServletResponse response);

    void assetsStatusDelayedExport(HttpServletResponse response);

    /**
     * 导出实时报表
     * @param reportParam
     * @param response
     */
    void exportRealTimeReport(MwCustomReportParam reportParam,HttpServletResponse response);

    /**
     * 导出历史报表
     * @param reportParam
     * @param response
     */
    void exportHistoryReport(MwCustomReportParam reportParam,HttpServletResponse response);
}
