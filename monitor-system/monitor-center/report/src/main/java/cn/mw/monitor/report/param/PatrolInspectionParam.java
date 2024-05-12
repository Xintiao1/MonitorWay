package cn.mw.monitor.report.param;

import lombok.Data;

import java.util.List;

/**
 * @ClassName PatrolInspectionParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/11/3 16:45
 * @Version 1.0
 **/
@Data
public class PatrolInspectionParam {

    private Integer dateType;

    private String startTime;

    private String endTime;

    private List<String> chooseTime;

    private String reportId;
}
