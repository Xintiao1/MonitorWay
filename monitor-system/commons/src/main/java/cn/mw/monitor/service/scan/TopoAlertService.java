package cn.mw.monitor.service.scan;

import cn.mw.monitor.service.scan.model.TaskStatus;
import cn.mw.monitor.service.scan.param.TopoNotifyParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;

public interface TopoAlertService<T> {
    static final String IP = "ip";
    static final String MODULE_KEY = "topo";
    void sendMessage(String message);
    Reply getNotifyEvent(TopoNotifyParam param) throws Exception;
    List<String> getDropDownList(String key);
    List<Map<String, String>> getRelationList(boolean isGroup);
    String saveRule(T param) throws Exception;
    void notifyTopoUpdate(String topoId);
    T editRule(T param) throws Exception;
    void topoDel(String topoId) throws Exception;
    boolean setTerminalEnable(String topoId ,String topoName ,boolean enable) throws Exception;
    boolean getTerminalEnable(String topoId);
    List<TaskStatus> getTaskStatus();
}
