package cn.mw.monitor.report.service;

import cn.mw.monitor.report.param.IpAddressReportParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName
 * @Description IP地址报表导出excel
 * @Author gengjb
 * @Date 2023/3/13 11:31
 * @Version 1.0
 **/
public interface IpAssressRepostExpoetService {

    void ipAddressReportExportExcel(IpAddressReportParam param, HttpServletRequest request, HttpServletResponse response);

    void ipAddressReportExportPdf(IpAddressReportParam param, HttpServletRequest request, HttpServletResponse response);
}
