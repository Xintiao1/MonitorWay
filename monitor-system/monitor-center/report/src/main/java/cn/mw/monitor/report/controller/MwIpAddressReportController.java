package cn.mw.monitor.report.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.report.param.IpAddressReportParam;
import cn.mw.monitor.report.service.IpAddressReportService;
import cn.mw.monitor.report.service.IpAssressRepostExpoetService;
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
 * @ClassName
 * @Description IP地址报表
 * @Author gengjb
 * @Date 2023/3/13 14:14
 * @Version 1.0
 **/
@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "IP地址报表接口", tags = "")
public class MwIpAddressReportController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MwIpAddressReportController.class.getName());

    @Autowired
    private IpAddressReportService ipAddressReportService;

    @Autowired
    private IpAssressRepostExpoetService ipAssressRepostExpoetService;

    /**
     * 巡检报告查询
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/ipadress/browse")
    @ResponseBody
    @ApiOperation(value = "获取IP地址报表使用率统计")
    public ResponseBase getIpAddressUtilization(@RequestBody IpAddressReportParam param) {
        Reply reply;
        try {
            reply = ipAddressReportService.getIpAddressReportData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("获取IP地址报表使用率统计失败{}", e);
            return setResultFail("获取IP地址报表使用率统计失败","获取IP地址报表使用率统计失败");
        }
        return setResultSuccess(reply);
    }


    /**
     * 二级目录报表
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/ipadressutilizationDto/browse")
    @ResponseBody
    @ApiOperation(value = "二级目录弹窗")
    public ResponseBase getIpAddressUtilizationDto(@RequestBody IpAddressReportParam param) {
        Reply reply;
        try {
            reply = ipAddressReportService.getIpAddressUtilizationDto(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("获取IP地址报表使用率统计失败{}", e);
            return setResultFail("获取IP地址报表使用率统计失败",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * Ip地址报表导出excel
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/ipadress/exportExcel")
    @ResponseBody
    @ApiOperation(value = "获取IP地址报表导出excel")
    public void getIpAddressReportExportExcel(@RequestBody IpAddressReportParam param, HttpServletRequest request, HttpServletResponse response) {
        try {
            ipAssressRepostExpoetService.ipAddressReportExportExcel(param,request,response);
        } catch (Throwable e) {
            logger.error("IP地址报表导出excel失败{}", e);
        }
    }

    /**
     * Ip地址报表导出PDF
     * @return
     */
    @MwPermit(moduleName = "report_manage")
    @PostMapping("/report/ipadress/exportPdf")
    @ResponseBody
    @ApiOperation(value = "获取IP地址报表导出PDF")
    public void getIpAddressReportExportPdf(@RequestBody IpAddressReportParam param, HttpServletRequest request, HttpServletResponse response) {
        try {
            ipAssressRepostExpoetService.ipAddressReportExportPdf(param,request,response);
        } catch (Throwable e) {
            logger.error("IP地址报表导出pdf失败{}", e);
        }
    }
}
