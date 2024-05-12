package cn.mw.monitor.api.dataview;

import lombok.Data;

@Data
public class LineView {
    private String source;
    private String sourceIfName;
    private String target;
    private String targetIfName;
    private String value;
}
