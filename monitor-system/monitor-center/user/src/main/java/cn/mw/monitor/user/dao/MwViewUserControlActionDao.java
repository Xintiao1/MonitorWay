package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.HashType;
import cn.mw.monitor.user.model.MwViewUserControlAction;

import java.util.List;

public interface MwViewUserControlActionDao {

    List<MwViewUserControlAction> selectList();

    MwViewUserControlAction selectByPrimaryKey(Integer userId);
}
