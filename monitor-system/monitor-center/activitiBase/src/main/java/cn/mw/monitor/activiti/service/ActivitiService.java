package cn.mw.monitor.activiti.service;

import cn.mw.monitor.activiti.entiy.MyProcess;
import cn.mw.monitor.activiti.entiy.MyTaskEntiy;
import cn.mw.monitor.activiti.entiy.MyTaskTable;
import cn.mw.monitor.activiti.entiy.TaskEntiy;
import cn.mw.monitor.activiti.param.ActivitiActParam;
import cn.mw.monitor.activiti.param.DeleteParam;
import cn.mw.monitor.activiti.param.ProcessParam;
import cn.mw.monitor.activiti.param.SearchProcessParam;
import cn.mw.monitor.service.activitiAndMoudle.entiy.ActivitiParam;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.activiti.dto.OneClickParam;
import cn.mw.monitor.bean.BaseParam;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/9/21 11:20
 * @Version 1.0
 */
public interface ActivitiService {
    public static final String BIND_METHOD = "BIND_METHOD";
    public static final String BIND_BEAN = "BIND_BEAN";
    public static final String BIND_PARAM = "BIND_PARAM";
    public static final String BIND_PARAM_CLASS = "BIND_PARAM_CLASS";

    /**
     * 获取流程模板列表
     * @param param
     * @return
     */
    Reply getModelList(BaseParam param);

    /**
     * 获取待办列表
     * @param param
     * @return
     */
    Reply getActList(BaseParam param);

    /**
     * 根据idList删除
     * @return
     */
    Reply batchDelByProcessIds(List<String> ids);
    Reply batchDel(List<String> ids);

    /**
     * 开启一个流程
     * @param key  流程key值
     * @param variables  流程变量
     * @return
     */
    Reply startWorkFlow(String key, Map<String,Object> variables);

    /**
     * 我的申请
     * @param param
     * @return
     */
    Reply myApplyList(BaseParam param);

    /**
     * 获取流程图执行节点
     * @param instanceId
     * @return
     */
    Reply getApplyStatus(String instanceId);

    /**
     * 根据流程实例id删除流程，并改变关联知识的流程状态
     * @param processId  流程实例id
     * @param knowledgeId  知识id
     * @return
     */
    Reply recallWorkFlow(String processId,String knowledgeId);

    /**
     * 审核一键通过
     * @param param
     * @return
     */
    Reply batchPass(OneClickParam param) throws Throwable;

    /**
     * 我的待办数量
     * @return
     */
    Reply countActList(Integer userId, Long count);

    /**
     *  根据userId 发送实时数据
     * @param userId
     * @param rejectedCount  手动添加的被驳回数量
     * @param auditCount 手动添加的带审核数量 （因为触发待审核监听发生在待审核记录到activiti之前）
     * @param passCount 手动添加的通过数量
     * @return
     */
    Reply getRealTimeUpdateData(Integer userId, Long rejectedCount,Long auditCount,Long passCount);

    /**
     *  根据userId 删除对应的实时工作流审核已通过数量
     * @param userId
     * @return
     */
    Reply deleteRedisPassCountByUserId(Integer userId);

    /**
     *  根据processId 查询任务批注
     * @param processId
     * @return
     */
    Reply getTaskCommentsByProcessId(String processId);

    /**
     * 根据流程实例获取流程图json数据
     * @param processId
     * @return
     */
    Reply getFlowViewData(String processId);

    /**
     * 流程创建
     * @param processParam
     * @return
     */
    Reply createProcess(ProcessParam processParam) throws Exception;

    /**
     * 流程查询
     * @param processParam
     * @return
     */
    Reply searchProcess(SearchProcessParam processParam) throws Exception;

    /**
     * 流程启动
     * @param processId
     * @return instanceId
     */
    String startProcess(String processId);

    String startProcess(String processId, Map<String, Object> variables);

    /**
     * 获取流程实例变量
     * @param instanceId
     * @return
     */
    Map getInstanceVariable(String instanceId);
    /**
     * 完成流程任务
     * @param taskId
     * @return
     */
    void completeTask(String taskId, String processId);


    /**
         *逻辑删除流程
     * @param deleteParam
     * @return
     */
    void deleteProcessById(DeleteParam deleteParam);

    Reply getTask(TaskEntiy taskEntiy);

    void activite(ActivitiActParam activitiParam);

    Reply getMyTask(MyTaskEntiy taskEntiy);

    Reply getProcess(MyProcess myProcess);

    Reply complete(MyProcess myProcess);

    Reply createProcessTwo(ProcessParam processParam);

    Reply disMoudle(ProcessParam processParam);

    Reply createMoudle(ProcessParam processParam);

    Reply candleMoudle(ProcessParam processParam);

    Reply addMoudleLine(ProcessParam processParam);

    Reply proccessListBrowse(SearchProcessParam searchParam);

    Reply getProcessInfo(TaskEntiy taskEntiy);

    Reply getMyTaskTable(MyTaskTable myTaskTable);

    Reply browseDelete(SearchProcessParam searchParam);

    Reply checkMyTask(MyProcess myProcess);
}
