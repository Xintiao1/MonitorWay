package cn.mw.monitor.path;

import java.util.List;

public interface PathManageCallback {
    List<GNode> genRootNodes();
    List<PrevPath> choosePendingDelPath(List<PrevPath> prevPaths ,PathNode pathNode);
    void recoverLines();
}
