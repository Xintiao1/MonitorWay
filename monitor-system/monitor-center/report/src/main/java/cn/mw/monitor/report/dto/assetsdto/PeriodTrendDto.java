package cn.mw.monitor.report.dto.assetsdto;

import lombok.Data;

import java.util.List;

@Data
public class PeriodTrendDto {
    private List<String> date;
    private List<Long> count1;
    private List<Integer> count;

    private String title;
    private String units;
    private Integer type;
}
