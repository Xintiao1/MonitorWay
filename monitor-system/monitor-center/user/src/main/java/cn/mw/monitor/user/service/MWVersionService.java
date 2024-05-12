package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;

public interface MWVersionService {
    /**
     * mw版本查询
     * @return
     */
    Reply selectVersion();
}
