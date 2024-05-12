package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;

public interface MWCustomPermService {
    Reply customModuleToRedis();

    Reply customNotCheckToRedis();

}
