package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwDbVersionDTO;

public interface MWVersionDao {
    /**
     * 查询mw版本信息
     * @return
     */
    MwDbVersionDTO selectVersion();
}
