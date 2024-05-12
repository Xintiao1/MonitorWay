package cn.mw.monitor.report.service;

import cn.mw.monitor.report.param.IpAddressReportParam;
import cn.mwpaas.common.model.Reply;

/**
 * @ClassName
 * @Description IP地址报表接口
 * @Author gengjb
 * @Date 2023/3/7 10:05
 * @Version 1.0
 **/
public interface IpAddressReportService {

    //获取IP地址段使用率统计
    Reply getIpAddressReportData(IpAddressReportParam addressReportParam);

    //查询导出数据
    Reply getIpAddressExportData(IpAddressReportParam addressReportParam);

    Reply getIpAddressUtilizationDto(IpAddressReportParam param);
}
