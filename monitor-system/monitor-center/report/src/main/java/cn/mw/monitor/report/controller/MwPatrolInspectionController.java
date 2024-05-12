package cn.mw.monitor.report.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.report.param.PatrolInspectionParam;
import cn.mw.monitor.report.service.MwPatrolInspectionService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName MwPatrolInspectionController
 * @Author gengjb
 * @Date 2022/11/1 10:20
 * @Version 1.0
 **/
@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "巡检报告接口", tags = "")
public class MwPatrolInspectionController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MwPatrolInspectionController.class.getName());

    @Autowired
   private MwPatrolInspectionService patrolInspectionService;

    /**
     * 巡检报告查询
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/patrol/browse")
    @ResponseBody
    @ApiOperation(value = "巡检报告查询")
    public ResponseBase selectPatrolInspection(@RequestBody PatrolInspectionParam param) {
        Reply reply;
        try {
            reply = patrolInspectionService.selectPatrolInspection(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("查询巡检报告失败","");
            }
        } catch (Throwable e) {
            logger.error("查询巡检报告失败{}", e);
            return setResultFail("查询巡检报告失败","");
        }
        return setResultSuccess(reply);
    }


    /**
     * 巡检报告导出word
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/patrol/export/word")
    @ResponseBody
    @ApiOperation(value = "巡检报告导出word")
    public void exportPatrolInspectionWord(@RequestBody PatrolInspectionParam patrolInspectionParam,HttpServletRequest request, HttpServletResponse response) {
        try {
            patrolInspectionService.exportWord(request,response,patrolInspectionParam);
        } catch (Throwable e) {
            logger.error("巡检报告导出word失败{}", e);
        }
    }


    /**
     * 巡检报告导出excel
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/patrol/export/excel")
    @ResponseBody
    @ApiOperation(value = "巡检报告导出excel")
    public void exportPatrolInspectionExcel(@RequestBody PatrolInspectionParam patrolInspectionParam,HttpServletRequest request, HttpServletResponse response) {
        try {
            patrolInspectionService.exportExcel(patrolInspectionParam,request,response);
        } catch (Throwable e) {
            logger.error("巡检报告导出excel{}", e);
        }
    }

    /**
     * 巡检报告导出excel
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/patrol/export/interfaceExcel")
    @ResponseBody
    @ApiOperation(value = "巡检报告导出接口excel")
    public void exportPatrolInspectionInterfaceExcel(@RequestBody PatrolInspectionParam patrolInspectionParam,HttpServletRequest request, HttpServletResponse response) {
        try {
            patrolInspectionService.exportInterfaceExcel(patrolInspectionParam,request,response);
        } catch (Throwable e) {
            logger.error("巡检报告导出接口excel{}", e);
        }
    }
}
