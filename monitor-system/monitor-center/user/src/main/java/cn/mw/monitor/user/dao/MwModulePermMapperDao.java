package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwModulePermMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwModulePermMapperDao {

    /**
     * 清除表数据
     */
    int clearMapper();
    /**
     * 新增模块权限映射信息
     */
    int insert(@Param("list")List<MwModulePermMapper> list);

    List<MwModulePermMapper> selectList();

}