package cn.mw.monitor.report.param;

import cn.mw.monitor.report.dto.assetsdto.RunTimeQueryParam;
import lombok.Data;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author syt
 * @Date 2021/9/29 20:37
 * @Version 1.0
 */
@Data
public class ReportWordParam {
    private Date dateStart;
    private Date dateEnd;

    private Integer dateType;

    private List<Integer> trendTypes = Arrays.asList(0,1,2);

    private List<RunTimeQueryParam> topQueryParams;

    private List<String> imgBase64;
}
