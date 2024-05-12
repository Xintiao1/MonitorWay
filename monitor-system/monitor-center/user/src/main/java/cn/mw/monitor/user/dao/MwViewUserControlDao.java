package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwViewUserControl;
import java.util.List;

public interface MwViewUserControlDao {

    List<MwViewUserControl> selectList();

    List<MwViewUserControl> selectByUserId(Integer userId);
}
