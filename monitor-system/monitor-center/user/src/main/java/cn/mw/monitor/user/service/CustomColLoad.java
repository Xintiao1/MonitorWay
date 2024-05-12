package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.event.EventListner;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/26
 */
public interface CustomColLoad extends EventListner {
    public List<Reply> processCustomColLoad(List<Integer> userIds);
    public List<Reply> processCustomColByModelLoad(List<Integer> userIds);
}
