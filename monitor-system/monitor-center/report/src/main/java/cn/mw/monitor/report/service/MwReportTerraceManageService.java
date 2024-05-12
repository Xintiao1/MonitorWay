package cn.mw.monitor.report.service;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.dto.assetsdto.IpReportSreach;
import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import cn.mw.monitor.report.param.*;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.dto.ServerHistoryDto;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @ClassName MwReportTerraceManageService
 * @Description 山鹰报表查询
 * @Author gengjb
 * @Date 2021/10/13 10:54
 * @Version 1.0
 **/
public interface MwReportTerraceManageService {

    /**
     * 查询资产报表信息
     * @return
     */
    Reply selectAssetsNews(QueryTangAssetsParam browseTangAssetsParam);

    Reply selectReportCPUNews(RunTimeQueryParam param);

    Reply selectReportLinkNews(TrendParam trendParam);

    Reply selectReportDiskUse(TrendParam trendParam);

//    Reply selectReportDiskUsable(TrendParam trendParam);

    Reply selectReportAssetsUsability(RunTimeQueryParam param,boolean addNew);

    void assetsNewsReportExport(List<AssetsNewsReportExportParam> assets, HttpServletResponse response);

    void cpuNewsReportExport(List<CpuNewsReportExportParam> params, HttpServletResponse response);

    void diskUseReportExport(List<DiskUseReportExportParam> params, HttpServletResponse response);

    void diskUseAbleReportExport(List<DiskUseAbleReportExportParam> params, HttpServletResponse response);

    void assetsUsabilityReportExport(List<MwAssetsUsabilityParam> params, HttpServletResponse response);

    void lineFlowReportExport(List<LineFlowReportParam> params, HttpServletResponse response);

    Reply selectAssetsReportTree(TrendParam param);

    Reply selectReportLineMpls(MwLineMplsParam param);

    Reply selectReportLineMplsPool(List<ServerHistoryDto> param);

    Reply selectLineMplsReportLineName();

    Reply selectReportLinkGrade();

    Reply selectReportLinkGradeData(MwLinkGradeParam param);

    Reply selectLinkHistoryFlow(ServerHistoryDto param);

    void lineMplsReportExport(List<MwLineMplsParam> params, HttpServletResponse response);


    Reply selectReportIpNews(IpReportSreach param);

    void ipReportExport(List<IpAddressReport> ipReportSreaches, HttpServletResponse response, Integer radio);

    Reply seleAllLink();

    Reply selectReportDown();

    Reply manualRunTimeTask(Integer reportId);

    //查询蓝月亮流量报表数据
    Reply selectLylLinkFlowData(TrendParam param);

    TimeTaskRresult manualRunTimeTaskTwo(String reportId);

    TimeTaskRresult manualRunTimeTaskThere(String reportId);
}
