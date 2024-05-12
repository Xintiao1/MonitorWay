package cn.mw.monitor.report.service;

import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.report.dto.linkdto.ExportLinkParam;
import cn.mw.monitor.report.dto.linkdto.InterfaceReportDtos;
import cn.mw.monitor.report.param.ExcelReportParam;
import cn.mw.monitor.report.param.ReportCountParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.param.EditorTimeParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/5/9 16:12
 */
public interface MwReportService {
    Reply creatReport(CreatAndUpdateReportParam creatAndUpdateReportParam);

    Reply getReportType();

    Reply getReportTimeTask();

    Reply getReportAction();

    Reply updateReport(CreatAndUpdateReportParam creatAndUpdateReportParam);

    Reply deleteReport(DeleteParam deleteParam);

    Reply selectReport(QueryReportParam queryReportParam);

    Reply getReportDetail(TrendParam trendParam);

    Reply selectById(String reportId);


    List<NetWorkDto> getNetTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime);

    List<DiskDto> getDiskTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime);

    List<CpuAndMemoryDtos> getCpuAndMemoryTrends(List<MwTangibleassetsTable> mwTangibleassetsDTOS, Long startTime, Long endTime);

    List<InterfaceReportDtos> getLinks(List<NetWorkLinkDto> netWorkLinkDtos, Long startTime, Long endTime);

    Reply editorTime(EditorTimeParam param);

    Reply selectTime(EditorTimeParam param);

    Reply selectDayType(EditorTimeParam param);

    Reply inputTime(EditorTimeParam inputParam);

    List<CpuAndMemoryDto> getCpuAndMemoryTrend(TrendParam trendParam);

    List<TrendDiskDto> getDiskTrend(TrendParam trendParam);

    List<TrendNetDto> getNetTrend(TrendParam trendParam);

    Reply getReportCount(ReportCountParam reportCountParam);

    Reply selectLinkHistory(TrendParam trendParam);

    Reply getHistoryByList(TrendParam param);

    Reply groupSelect(TrendParam param);

    void exportLink(ExportLinkParam uParam, HttpServletResponse response);

    void export(ExcelReportParam uParam, HttpServletResponse response);

    Reply selectLinkEditDropdown(TrendParam interfaceID);


    Reply getRunTimeReportOfAeest(RunTimeQueryParam param);

    Reply getRunTimeItemUtilization(RunTimeQueryParam param);

    /**
     * 查询数据（优化后）
     * @param param
     * @param aNew
     * @return
     */
    Reply getRunTimeItemOptimizeUtilization(RunTimeQueryParam param, boolean addNew, boolean cacheTrue);

    Reply getRunTimeItem(RunTimeQueryParam param);

    Reply getRunTimeAssetUtilization(RunTimeQueryParam param);

    Reply getRunTimeReportTrend(RunTimeQueryParam param);

    Reply getOptimizeRunTimeReportTrend(RunTimeQueryParam param);

    Reply doptimize(RunTimeQueryParam param);

    Map<String,List<RunTimeItemValue>> getInterFaceAndDisk(RunTimeQueryParam param);
}
