package cn.mw.monitor.service.server.api.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2021/7/27 14:28
 * @Version 1.0
 */
@Data
public class MyMonitorExportParam extends LineChartDTO {
    //数据颗粒度
    private int delay;
}
