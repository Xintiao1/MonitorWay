package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.HashType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HashTypeDao {

    List<HashType> selectList();

    HashType selectById(@Param("id")String id);

}
