package cn.mw.monitor.report.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwLineReportExportParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/10/28 16:42
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MwLineReportExportParam {

    private List<Map<String,String>> receiveMaxData;

    private List<Map<String,String>> receiveMinData;

    private List<Map<String,String>> sendMaxData;

    private List<Map<String,String>> sendMinData;

    private String name;

}
