package cn.mw.monitor.service.ssh;

import cn.mwpaas.common.model.Reply;

import java.io.IOException;

/**
 * @author lumingming
 * @createTime 2022728 15:23
 * @description 测试
 */
public interface JavaSshService {
    Reply execute(TopoSshExecParam topoSshExecParam) throws IOException;

    Reply closeRun(TopoSshExecParam topoSshExecParam);

    Reply getOut(TopoSshExecParam topoSshExecParam) throws InterruptedException, IOException;
}
