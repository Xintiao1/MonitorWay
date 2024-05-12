package cn.mw.monitor.script.dao;

import cn.mw.monitor.script.entity.ScriptExecEntity;
import cn.mw.monitor.script.entity.ScriptManageEntity;
import cn.mw.monitor.script.param.HomeworkHis;
import cn.mw.monitor.script.param.TransAssets;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author gui.quanwang
 * @className ScriptManageDao
 * @description 脚本管理Dao层
 * @date 2022/4/8
 */
public interface ScriptExecDao extends BaseMapper<ScriptExecEntity> {

    /**
     * 查询脚本管理历史执行模糊数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchScriptExecData();

    /**
     * 获取作业步骤列表数据
     *
     * @param homeworkVersionId 作业版本ID
     * @return
     */
    List<ScriptExecEntity> getHomeworkStepList(@Param(value = "versionId") int homeworkVersionId);

    /**
     * 获取执行资产数据列表
     *
     * @param execId 执行ID
     * @return
     */
    List<TransAssets> getTransAssetsList(@Param(value = "execId") int execId);

    /**
     * 获取作业步骤结果数据
     *
     * @param homeworkVersionId 作业版本ID
     * @param execId            执行ID
     * @return
     */
    List<ScriptExecEntity> getHomeworkStepResultList(@Param(value = "versionId") int homeworkVersionId,
                                                     @Param(value = "execId") int execId);

    /**
     * 获取未完成执行数量
     *
     * @param homeworkVersionId 版本ID
     * @return
     */
    int countUnFinishedExec(@Param(value = "versionId") int homeworkVersionId);


    /**
     * 获取失败的执行数量
     *
     * @param homeworkVersionId 版本ID
     * @return
     */
    int countErrorExec(@Param(value = "versionId") int homeworkVersionId);

    /**
     * 计算耗费时间
     *
     * @param homeworkVersionId 版本ID
     * @return
     */
    int countCostTime(@Param(value = "versionId") int homeworkVersionId);

    void deletebyIds(@Param("ids")  List<Integer> ids);


    List<ScriptExecEntity> selectIsNotDelete(@Param("param") HomeworkHis param);

    void updateTimeOver();
}
