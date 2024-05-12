package cn.mw.monitor.service.scan.model;

public class CheckTopoStatus {
    //是否刷新拓扑列表
    boolean isRefreshTopoList;

    //是否继续检查拓扑执行状态
    boolean isCheckTopoStatus;

    public boolean isRefreshTopoList() {
        return isRefreshTopoList;
    }

    public void setRefreshTopoList(boolean refreshTopoList) {
        isRefreshTopoList = refreshTopoList;
    }

    public boolean isCheckTopoStatus() {
        return isCheckTopoStatus;
    }

    public void setCheckTopoStatus(boolean checkTopoStatus) {
        isCheckTopoStatus = checkTopoStatus;
    }
}
