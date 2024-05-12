package cn.mw.monitor.report.service;

import cn.mw.monitor.report.param.MwCustomReportParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author gengjb
 * @description 自定义指标报表数据查询
 * @date 2023/10/12 16:21
 */
public interface MwCustomReportService {

    Reply getCustomReportInfo(MwCustomReportParam reportParam);

    /**
     * 获取指标数据下拉
     * @return
     */
    Reply getReportIndexDropDown();
}
