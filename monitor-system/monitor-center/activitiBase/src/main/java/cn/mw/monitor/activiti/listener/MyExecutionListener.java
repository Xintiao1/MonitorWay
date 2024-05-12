package cn.mw.monitor.activiti.listener;

import cn.mw.monitor.activiti.dto.ProcessVariablesNameEnum;
import cn.mw.monitor.activiti.service.ActivitiService;
import cn.mw.monitor.activiti.util.ActivitiUtils;
import cn.mw.monitor.activiti.util.InitServiceUtils;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.Comment;

import java.util.List;

/**
 * @author syt
 * @Date 2020/10/19 14:15
 * @Version 1.0
 */
@Slf4j
public class MyExecutionListener implements ExecutionListener, TaskListener {
    //监听even事件名
    private static final String END = "end";
    private static final String TAKE = "take";
    private static final String ASSIGNMENT = "assignment";


    private ActivitiService activitiDemoService = InitServiceUtils.getInstance().getActivitiDemoService();

    private ILoginCacheInfo iLoginCacheInfo = InitServiceUtils.getInstance().getILoginCacheInfo();

    @Override
    public void notify(DelegateExecution delegateExecution) {
        String eventName = delegateExecution.getEventName();
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        String userName = (String) delegateExecution.getVariable(ProcessVariablesNameEnum.USER.getName());//获取流程发起人
        if (END.equals(eventName)) {
            //这是流程结束时触发的
            if (iLoginCacheInfo.getCacheInfo(userName) != null) { //说明流程发起人在线
                Integer initUserId = iLoginCacheInfo.getCacheInfo(userName).getUserId();
                activitiDemoService.countActList(initUserId, 1L);
            }
            activitiDemoService.getRealTimeUpdateData(userId, 0L, -1L, 0L);
        } else if (TAKE.equals(eventName)) {
            Long rejectedCount = 0L;
            //这是审批被驳回时触发的
            if (iLoginCacheInfo.getCacheInfo(userName) != null) { //说明流程发起人在线
                Integer initUserId = iLoginCacheInfo.getCacheInfo(userName).getUserId();
                if (initUserId != userId) {//发布的用户受理人不是同一个人时
                    rejectedCount = 1L;
                }
                activitiDemoService.getRealTimeUpdateData(initUserId, rejectedCount, 0L, 0L);
            }
            activitiDemoService.getRealTimeUpdateData(userId, -rejectedCount, 0L, 0L);
        }
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        Long rejectedCount = 0L;
        if (ASSIGNMENT.equals(eventName)) {
            String assignee = delegateTask.getAssignee();//获取下一个节点的受理人
            if (iLoginCacheInfo.getCacheInfo(assignee) != null) { //说明下一步受理人在线
                Integer receiverUserId = iLoginCacheInfo.getCacheInfo(assignee).getUserId();
                if (receiverUserId != userId) {//发布的用户和下一步受理人不是同一个人时
                    rejectedCount = -1L;
                }
                activitiDemoService.getRealTimeUpdateData(receiverUserId, 0L, 1L, 0L);//给受理人发消息通知
            }
            try {
                List<Comment> comments = ActivitiUtils.getTaskCommentsByProcessId(delegateTask.getProcessInstanceId());
                if (comments != null && comments.size() > 0) {//说明是被驳回的重新发布
                    activitiDemoService.getRealTimeUpdateData(userId, rejectedCount, 0L, 0L);
                }
            } catch (Exception e) {
                log.error("fail to getTaskCommentsByProcessId()  params:{} cause:{}", delegateTask.getProcessInstanceId(), e);
            }
        }
    }
}
