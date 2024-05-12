package cn.mw.monitor.event;

import cn.mwpaas.common.model.Reply;

import java.util.List;

public interface EventListner {
    List<Reply> handleEvent(Event event) throws Throwable;
}
