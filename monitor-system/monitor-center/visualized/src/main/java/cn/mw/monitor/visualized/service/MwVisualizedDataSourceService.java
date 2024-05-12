package cn.mw.monitor.visualized.service;

import cn.mw.monitor.visualized.param.MwVisualizedIndexQueryParam;

import java.util.List;

/**
 * @ClassName MwVisualizedDataSourceService
 * @Author gengjb
 * @Date 2022/4/26 11:00
 * @Version 1.0
 **/
public interface MwVisualizedDataSourceService {

    int getDataSource();

    Object getData(MwVisualizedIndexQueryParam indexQueryParam);
}
