package cn.mw.monitor.user.dao;


import cn.mw.monitor.user.model.MwPermission;

import java.util.List;

public interface MwPermissionDao {

    /**
     * 查询权限信息列表
     */
    List<MwPermission> selectList();

}