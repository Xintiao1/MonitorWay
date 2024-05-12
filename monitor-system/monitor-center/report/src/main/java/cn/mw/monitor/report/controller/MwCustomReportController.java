package cn.mw.monitor.report.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.report.param.MwCustomReportParam;
import cn.mw.monitor.report.service.MWRreportExportService;
import cn.mw.monitor.report.service.MwCustomReportService;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * @author gengjb
 * @description 自定义指标报表
 * @date 2023/10/27 15:35
 */
@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "自定义指标报表", tags = "")
public class MwCustomReportController  extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MwCustomReportController.class.getName());

    @Autowired
    private MwCustomReportService customReportService;

    @Autowired
    private MWRreportExportService exportService;

    /**
     * 自定义报表数据查询
     * @param reportParam
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/custom/browse")
    @ResponseBody
    @ApiOperation(value = "自定义报表数据查询")
    public ResponseBase selectCustomReportInfo(@RequestBody MwCustomReportParam reportParam) {
        Reply reply;
        try {
            reply = customReportService.getCustomReportInfo(reportParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("自定义报表数据查询失败","");
            }
        } catch (Throwable e) {
            logger.error("MwCustomReportController{} selectCustomReportInfo", e);
            return setResultFail("自定义报表数据查询失败","");
        }
        return setResultSuccess(reply);
    }


    /**
     * 自定义报表指标下拉
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/custom/index/dropDown")
    @ResponseBody
    @ApiOperation(value = "自定义报表指标下拉")
    public ResponseBase getReportIndexDropDown() {
        Reply reply;
        try {
            reply = customReportService.getReportIndexDropDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("自定义报表指标下拉失败","");
            }
        } catch (Throwable e) {
            logger.error("MwCustomReportController{} selectCustomReportInfo", e);
            return setResultFail("自定义报表指标下拉失败","");
        }
        return setResultSuccess(reply);
    }

    /**
     * 实时报表导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/custom/realTime/export")
    @ResponseBody
    @ApiOperation(value = "实时报表导出")
    public void customRealTimeReportExport(@RequestBody MwCustomReportParam reportParam,HttpServletResponse response) {
        try {
            exportService.exportRealTimeReport(reportParam,response);
        } catch (Throwable e) {
            logger.error("MwCustomReportController{} customRealTimeReportExport() ERROR::", e);
        }
    }

    /**
     * 历史报表导出
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/custom/history/export")
    @ResponseBody
    @ApiOperation(value = "历史报表导出")
    public void customHistoryReportExport(@RequestBody MwCustomReportParam reportParam,HttpServletResponse response) {
        try {
            exportService.exportHistoryReport(reportParam,response);
        } catch (Throwable e) {
            logger.error("MwCustomReportController{} customHistoryReportExport() ERROR::", e);
        }
    }
}
