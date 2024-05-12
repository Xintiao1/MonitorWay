package cn.mw.monitor.timetask.dao;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.timetask.entity.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author lumingming
 * @createTime 28 10:13
 * @description
 */
@Mapper
@Component(value = "MwNcmTimetaskTimePlanMapper")
public interface MwNcmTimetaskTimePlanMapper extends BaseMapper<MwNcmTimetaskTimePlan> {


    String llls();

    List<Tree> getReportParent();

    List<Tree> getReportChild(@Param("modelName") String modelName, @Param("strings")List<String> strings, @Param("search")String search);

    List<Tree> getIPListChild(@Param("modelName") String modelName, @Param("strings")List<String> strings, @Param("search")String search);

    void insertTimeTask(@Param("newTimeTask") NewTimeTask newTimeTask);


    void insertTimeTaskMapperObject(@Param("newTimeTaskId")  String id, @Param("newtimetaskMapperObject") List<NewtimetaskMapperObject> newtimetaskMapperObject);

    String getModelName(@Param("modelId")  Integer modelId);

    void insertTimeTaskMapperTime(@Param("newTimeTaskId") String id, @Param("newtimetaskMapperTimes") List<NewtimetaskMapperTime> newtimetaskMapperTimes);

    List<NewTimeTask> selectTimeAllTask(@Param("newTimeTask") NewTimeTask newTimeTask);

    List<String> getObjectId(@Param("id")  String id);

    List<String> getTimePanId(@Param("id") String id);

    List<MwNcmTimetaskTimePlan> getTimePlan(@Param("id") String id);

    void deleteMapperObjectId(@Param("id") String id);

    void deleteMapperTimeId(@Param("id") String id);

    void updateNewTimeTask(@Param("newTimeTask") NewTimeTask newTimeTask);

    List<MwNcmTimetaskTimePlanRun> selectTimePlan(@Param("id") String id);

    TimetaskActrion getAction( @Param("newtimetaskId")  String newtimetaskId);

    void insertTimeTakHis(@Param("objects") List<TimeTaskRresult> objects);

    List<MwNcmTimetaskTimePlanRun> selectTimePlanbyTimeid(@Param("id")Integer id);

    void deleteNewTimeTask(String k);

    List<TimeTaskRresult> getHistory(@Param("newtimetaskId") String newtimetaskId);

    void timeAllTaskHisDelete(@Param("newTimeTask") DeleteTimeTask newTimeTask);

    List<Tree> getTangibleassets(@Param("modelName") String modelName, @Param("strings")List<String> strings, @Param("search")String search);

    String getModelNamebyActionId(@Param("actionId") Integer actionId);

    List<MwNcmTimetaskTimePlan> listAll();

    void insertTaskTime(@Param("param") MwNcmTimetaskTimePlan param);

    void deleteByIdMy(@Param("id") Integer id);

    void updateByIdMy(MwNcmTimetaskTimePlan param);

    TimetaskActrion getActionById(@Param("actionId")  Integer actionId);

    List<Tree> getAutoProgram(@Param("modelName") String modelName, @Param("strings")List<String> strings, @Param("search")String search);
}
