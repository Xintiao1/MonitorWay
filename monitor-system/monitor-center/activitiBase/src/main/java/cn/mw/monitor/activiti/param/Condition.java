package cn.mw.monitor.activiti.param;

import lombok.Data;

@Data
public class Condition {
    private int type;
    private int columnId;
    private String showName;
}
