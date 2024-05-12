package cn.mw.monitor.visualized.dao;

import cn.mw.monitor.visualized.dto.MwVisualizedModuleRelationDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName MwVisualizedModuleRelationDao
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/12/5 11:52
 * @Version 1.0
 **/
public interface MwVisualizedModuleRelationDao {

    //添加可视化模块关联关系记录
    int insertVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto);

    //修改可视化模块关联关系记录
    int updateVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto);

    //查询可视化模块关联关系记录
    List<MwVisualizedModuleRelationDto> selectVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto);

    //删除可视化模块关联关系记录
    int deleteVisualizedModuleRelation(@Param("ids") List<Integer> ids);
}
