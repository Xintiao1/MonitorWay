package cn.mw.monitor.path;

import java.util.List;

public class DefaultPathCallback implements PathManageCallback {
    @Override
    public List<GNode> genRootNodes() {
        return null;
    }

    @Override
    public List<PrevPath> choosePendingDelPath(List<PrevPath> prevPaths ,PathNode pathNode) {
        return PathNodeUtils.choosePendingDelPath(prevPaths);
    }

    @Override
    public void recoverLines() {

    }


}
