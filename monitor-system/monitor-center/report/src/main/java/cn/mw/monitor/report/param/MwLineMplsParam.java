package cn.mw.monitor.report.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwLineMplsParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/26 9:26
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MwLineMplsParam {

    //线路名称
    private List<String> lineName;

    //时间类型
    private Integer dateType;

    //自定义时间
    private List<String> chooseTime;

    private Integer type;

   List<MwLineReportExportParam> params;
}
