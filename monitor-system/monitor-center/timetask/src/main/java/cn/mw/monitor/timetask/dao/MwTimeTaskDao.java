package cn.mw.monitor.timetask.dao;



import cn.mw.monitor.timetask.entity.*;
import org.apache.ibatis.annotations.Param;


import java.util.List;
import java.util.Map;

public interface MwTimeTaskDao {

    int deleteDownloadHis(List<MwTimeTaskDownloadHis> qParam);

    //查询执行历史
    List<MwTimeTaskDownloadHis> selectListHis(Map priCriteria);

    //根据主键查询
    MwTimeTaskTable selectOne(AddTimeTaskParam qParam);

    //查询
    List<MwTimeTaskTable> selectList(Map priCriteria);

    List<MwTimeTaskTable> selectAllList();


    //批量删除
    int deleteBatch(List<AddTimeTaskParam> list);

    //修改定时任务
    int update(AddTimeTaskParam record);

    //修改定时任务
    int updateSomeThree(MwTimeTaskTable record);


    //新增定时任务
    int insert(AddTimeTaskParam record);

    //删除查看历史
    int deleteHisList(@Param("timeId") Integer id);

    //删除关联配置
    int deleteConfig(@Param("timeId") Integer id);

    int insertConfig(@Param("list") List<MwTimeTaskConfigMapper> list, @Param("timeId") Integer id);

    List<MwTimeTaskTypeMapper> selectTypeList(@Param("type")  Integer type);

    List<TimetaskModel> getTree(@Param("type") Integer type, String sreach);

    /**
     * 根据模块ID获取数据
     * @param id 模块ID
     * @return
     */
    List<TimetaskActrion> findTreeAction(int id);

    //查询模块actionIds
    List<Integer> selectActionIdsBymodelId(@Param("modelId") Integer modelId);
}
