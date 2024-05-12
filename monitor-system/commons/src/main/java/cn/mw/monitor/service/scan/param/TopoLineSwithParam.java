package cn.mw.monitor.service.scan.param;

import cn.mw.monitor.bean.DataPermissionParam;
import lombok.Data;

@Data
public class TopoLineSwithParam {
    private String topoId;
    private Boolean enableLine;
}
