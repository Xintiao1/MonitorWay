package cn.mw.monitor.service.scan.param;

import lombok.Data;

@Data
public class TopoNotifyParam {
    private String topoId;
    private boolean first = false;
}
