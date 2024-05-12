package cn.mw.monitor.alert.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MWAlertRule implements Serializable {

    private Integer conditionId;

    private Integer actionId;

    private Integer typeId;

    private String type;

    private String subType;

    private String operator;
    private String cycleTime;
    private String cycleSeries;
    private String opt;
    private String condRule;

    private String value;

    private Integer isLast;

    private Integer times;

}
