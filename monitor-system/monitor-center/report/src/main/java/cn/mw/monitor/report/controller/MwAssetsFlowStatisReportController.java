package cn.mw.monitor.report.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.report.service.MwAssetsFlowStatisReportService;
import cn.mw.monitor.service.netflow.param.NetflowSearchParam;
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
 * @description  资产流量统计报表web
 * @date 2023/8/28 15:27
 */
@RequestMapping("/mwapi/report")
@Slf4j
@Controller
@Api(value = "资产流量统计报表", tags = "")
public class MwAssetsFlowStatisReportController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MwAssetsFlowStatisReportController.class.getName());

    @Autowired
    private MwAssetsFlowStatisReportService flowStatisReportService;


    @MwPermit(moduleName = "report_manage")
    @PostMapping("/assetsFlow/browse")
    @ResponseBody
    @ApiOperation(value = "资产流量统计报表查询")
    public ResponseBase getAssetsFlowInfo(@RequestBody NetflowSearchParam netflowSearchParam) {
        Reply reply;
        try {
            reply = flowStatisReportService.getAssetsFlowInfo(netflowSearchParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("资产流量统计报表查询失败","");
            }
        } catch (Throwable e) {
            logger.error("MwAssetsFlowStatisReportController{} getAssetsFlowInfo()", e);
            return setResultFail("资产流量统计报表查询失败","");
        }
        return setResultSuccess(reply);
    }



    @MwPermit(moduleName = "report_manage")
    @PostMapping("/assetsFlow/exportExcel")
    @ResponseBody
    @ApiOperation(value = "资产流量统计报表导出excel")
    public void exportAssetsFlowReport(@RequestBody NetflowSearchParam netflowSearchParam,HttpServletResponse response) {
        try {
            flowStatisReportService.exportAssetsFlowInfo(netflowSearchParam,response);
        } catch (Throwable e) {
            logger.error("MwAssetsFlowStatisReportController{} exportAssetsFlowReport(){}", e);
        }
    }

}
