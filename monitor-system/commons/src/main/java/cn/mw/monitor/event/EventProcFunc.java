package cn.mw.monitor.event;

import cn.mwpaas.common.model.Reply;

import java.util.List;

@FunctionalInterface
public interface EventProcFunc {
    List<Reply> process(Event event);
}
