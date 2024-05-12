package cn.mw.monitor.report.service.impl;

import cn.mw.monitor.report.dto.TrendDiskDto;
import cn.mw.monitor.report.dto.TrendParam;

import java.util.List;

@FunctionalInterface
public interface DiskNewsCallBack {
    List<TrendDiskDto> getDiskNews(TrendParam trendParam);
}
