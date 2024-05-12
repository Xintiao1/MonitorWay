package cn.mw.monitor.api.dataview;

import lombok.Data;

@Data
public class NodeView {
    private String name;
    private String logo;
    private int category = NodeConstants.NOT_ROOT;
    private boolean state = NodeConstants.NORMAL;
    private int symbolSize = 90;
}
