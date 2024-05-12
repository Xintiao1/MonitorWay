package cn.mw.monitor.report.dto;

import lombok.Data;

/**
 * @ClassName
 * @Description ToDo
 * @Author gengjb
 * @Date 2023/6/2 15:13
 * @Version 1.0
 **/
@Data
public class MwReportHistoryValueDto {
    private Double value;
    private Long clock;
    private String itemid;
    private String ns;
}
