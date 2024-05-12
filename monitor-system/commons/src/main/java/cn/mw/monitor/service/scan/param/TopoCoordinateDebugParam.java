package cn.mw.monitor.service.scan.param;

import lombok.Data;
import java.util.List;

@Data
public class TopoCoordinateDebugParam {
    private boolean isShowIsolatedNode;
    private List<TopoPosNodeDebugParam> nodes;
    private List<TopoPosLineDebugParam> lines;
}
