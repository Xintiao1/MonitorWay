package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.report.dto.TrendParam;
import com.github.pagehelper.PageInfo;

@FunctionalInterface
public interface DiskUseReportCacheDataFunc {
    PageInfo getDiskUseReportCacheData(DateTypeEnum dateType, TrendParam param);
}
