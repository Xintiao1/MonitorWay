package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;

import java.util.Map;

public interface MwModuleService {

    /**
     * 重置模块权限映射信息
     */
    Reply modulePermReset();
    /**
     * 重置角色模块权限映射信息
     */
    Reply roleModulePermMapperReset(Integer roleId);
    /**
     * 获取角色模块权限映射信息
     */
    Reply roleModulePermMapperBrowse(Integer roleId);

    /**
     * 获取模块信息
     */
    Reply getModuleInfo(Map criteria);
    /**
     *
     */
    String getModulePermKey(String url);

}
