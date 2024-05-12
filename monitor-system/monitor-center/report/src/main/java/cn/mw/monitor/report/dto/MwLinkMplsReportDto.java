package cn.mw.monitor.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwLinkMplsReportDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/27 15:39
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwLinkMplsReportDto {

    //机构或者标签名称
    private String titleName;

    //单位
    private String unit;

    //数据
    private List<Map<String,String>> data;

    //日期区间
    private String dateRegion;
}
