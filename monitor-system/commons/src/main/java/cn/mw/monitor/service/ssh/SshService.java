package cn.mw.monitor.service.ssh;

import cn.mwpaas.common.model.Reply;

public interface SshService {
    Reply execute(TopoSshExecParam topoSshExecParam) throws Throwable;
    Reply getResponse(TopoSshExecParam topoSshExecParam);
}
