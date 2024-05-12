package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MWPassCompType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MWPasswCompTypeDao {

    /**
     * 查询密码复杂类型列表
     */
    List<MWPassCompType> selectList();
    /**
     * 根据id查询密码复杂类型信息
     */
    MWPassCompType selectById(@Param("id")Integer id);

}
