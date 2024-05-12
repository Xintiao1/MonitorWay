package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwModule;
import cn.mw.monitor.user.model.MwModulePermMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwModuleDao {

    int insert(MwModule record);

    @Deprecated
    int deleteByPrimaryKey(@Param("id") Integer id);

    @Deprecated
    MwModule selectByPrimaryKey(@Param("id") Integer id);

    @Deprecated
    int updateByPrimaryKeySelective(MwModule record);

    @Deprecated
    MwModule selectByUrl(@Param("url") String url);
    /**
     * 查询模块信息列表
     */
    List<MwModule> selectList();


    /**
     * 根据模块id查询模块深度和节点id
     */
    Map<String, Object> selectDeepNodesById(@Param("pid") Integer pid);

    int updateIsNoteById(@Param("pid") Integer pid, @Param("isNote") boolean isNote);

    /**
     * 查询新增模块的id自增序列号
     */
    Integer selectMaxModuleId();

    /*
    * 根据模块id查询模块信息
    * */
    MwModule selectModuleById(Integer id);

    /*
     * 查询模块信息
     */
    List<MwModule> selectModule(Map map);


    /**
     * 根据pid查询模块数量
     */
    Integer countModuleByPid(@Param("id") Integer id);

    Integer deleteModuleByIds(@Param("ids") List<Integer> ids);

    Integer updateModule(MwModule mwModule);

    @Deprecated
    Integer insertModulePerm(@Param("list") List<MwModulePermMapper> list);

    Integer deleteModulePermByIds(@Param("ids") List<Integer> ids);}