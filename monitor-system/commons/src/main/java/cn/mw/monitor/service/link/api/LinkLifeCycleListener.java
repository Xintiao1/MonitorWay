package cn.mw.monitor.service.link.api;

import cn.mw.monitor.service.link.param.AddAndUpdateParam;

import java.util.List;

public interface LinkLifeCycleListener {
    void add(AddAndUpdateParam addAndUpdateParam);
    void modify(AddAndUpdateParam addAndUpdateParam);
    void delete(List<String> linkIds);
}
