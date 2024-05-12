package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwNotCheckUrl;

import java.util.List;

public interface MwNotCheckUrlDao {
    @Deprecated
    int deleteByPrimaryKey(Integer id);

    @Deprecated
    int insert(MwNotCheckUrl record);

    @Deprecated
    MwNotCheckUrl selectByPrimaryKey(Integer id);

    @Deprecated
    int updateByPrimaryKeySelective(MwNotCheckUrl record);

    List<MwNotCheckUrl> selectList();
}
