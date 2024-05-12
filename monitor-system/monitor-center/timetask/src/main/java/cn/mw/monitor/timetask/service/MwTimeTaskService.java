package cn.mw.monitor.timetask.service;



import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.timetask.entity.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface MwTimeTaskService {
    TimeProcessResult refreshServers(List<MwNcmTimetaskTimePlanRun> param);

    //编辑前查询
    Reply editorBrowse(AddTimeTaskParam param);

    //查看执行历史
    Reply selectListHis(QueryTimeTaskParam param);

    List<MwTimeTaskTable> selectList();

    Reply selectList(QueryTimeTaskParam param);

    //删除定时任务
    Reply delete(List<AddTimeTaskParam> record) throws Exception;

    //修改定时任务
    Reply update(AddTimeTaskParam record) throws Exception;

    //添加定时任务
    Reply insert(AddTimeTaskParam record) throws Exception;

    Reply getTypeList(Integer type);

    Reply browseHisFile(MwTimeTaskDownloadHis param);

    void downHisFile(MwTimeTaskDownloadHis param, HttpServletResponse response);

    Reply deleteHis(List<MwTimeTaskDownloadHis> param);

    Reply getTree(TimeTaskBase param);

    Reply insertTimeCreate(MwNcmTimetaskTimePlan param);

    Reply getTransferStationTree(Transfer transfer);


    Reply createTimeAllTask(NewTimeTask newTimeTask);

    Reply timeAllTaskBrows(NewTimeTask newTimeTask);

    Reply timeAllTaskObjectBrows(NewTimeTask newTimeTask);

    Reply timeAllTaskObjectEditorBrow(NewTimeTask newTimeTask);

    void action(Integer id,String action);

    Reply timeAllTaskDelete(DeleteTimeTask newTimeTask);

    Reply getHistory(Transfer transfer);

    Reply timeAllTaskHisDelete(DeleteTimeTask newTimeTask);

    void runNewTime(Transfer transfer);
}
