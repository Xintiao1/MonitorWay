package cn.mw.monitor.api.dataview;

import lombok.Data;

@Data
public class TopoIfView {
    private int id;
    private String conLevel;
    private IfView upIf;
    private boolean upFlow = false;
    private IfView downIf;
    private boolean downFlow = false;
}
