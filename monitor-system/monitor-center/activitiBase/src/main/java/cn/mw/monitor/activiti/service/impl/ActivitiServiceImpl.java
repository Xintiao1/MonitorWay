package cn.mw.monitor.activiti.service.impl;

import cn.mw.monitor.activiti.dao.ProcessDao;
import cn.mw.monitor.activiti.entiy.*;
import cn.mw.monitor.activiti.entiy.OA.NotifyTodoRemoveContext;
import cn.mw.monitor.activiti.entiy.OA.NotifyTodoSendContext;
import cn.mw.monitor.activiti.model.ProcessParamView;
import cn.mw.monitor.activiti.model.ProcessView;
import cn.mw.monitor.activiti.param.*;
import cn.mw.monitor.activiti.util.BpmnModelUtils;
import cn.mw.monitor.activiti.util.TestOA;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.common.util.SeverityUtils;
import cn.mw.monitor.service.activiti.param.OperMoudleReturn;
import cn.mw.monitor.service.activitiAndMoudle.ActivitiSever;
import cn.mw.monitor.service.activitiAndMoudle.KenwSever;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.activitiAndMoudle.entiy.ActivitiParam;
import cn.mw.monitor.service.model.param.MwInstanceCommonParam;
import cn.mw.monitor.service.model.param.MwModelPropertyInfo;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.*;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.UpdateDTO;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.service.MwModuleService;
import cn.mw.monitor.user.service.MwRoleService;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.websocket.WebSocketGetCount;
import cn.mw.monitor.activiti.dto.*;
import cn.mw.monitor.activiti.service.ActivitiService;
import cn.mw.monitor.activiti.util.ActivitiUtils;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.knowledgeBase.api.MwKnowledgeService;
import cn.mw.monitor.service.knowledgeBase.model.MwKnowledgeBaseTable;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.util.lucene.LuceneUtils;
import cn.mw.monitor.util.lucene.dto.LuceneFieldsDTO;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2020/9/21 11:24
 * @Version 1.0
 */
@Service
@Slf4j
public class ActivitiServiceImpl implements ActivitiService, ActivitiSever {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/activiti/service/impl/ActivitiServiceImpl");
    private static final String COMPLETED = "已完成";
    private static final String IMPLEMENTING = "执行中";
    private static final String REJECTED = "被驳回";
    private static final Integer ACTIVITI_RUNNING_STATUS = 1;
    private static final Integer ACTIVITI_REVOCATION_STATUS = 2;
    private static final Integer ACTIVITI_REJECTED_STATUS = 3;
    private static final Integer ACTIVITI_COMPLETED_STATUS = 4;

    private static final String WEBSOCKET_MESSAGE_COUNT = "websocket_messageCount";

    //未勾选返回给前端的错误信息
    private static final String ERRORINFO = "未勾选";
    //项目管理员名称
    private static final String ADMIN = "admin";


    @Value("${ersuo.oa.url:nourl}")
    private String OAUrl;
    @Value("${ersuo.oa.admin:nourl}")
    private String OAAdmin;
    @Value("${ersuo.oa.password:nourl}")
    private String OAPassword;
    @Value("${ersuo.oa.maoweiurl:nourl}")
    private String maoweiUrl;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private MWMessageService mwMessageService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ModelServer modelSever;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MwModelCommonService mwModelCommonService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwKnowledgeService mwKnowledgeService;

    @Autowired
    private WebSocketGetCount webSocketGetCount;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private MWUserCommonService mwUserCommonService;

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    MwRoleService mwRoleService;

    @Autowired
    private ProcessEngine processEngine;

    @Resource
    private ProcessDao processDao;

    @Autowired
    private KenwSever kenwSever;


    @Override
    public Reply getModelList(BaseParam param) {
        try {
            long count = repositoryService.createProcessDefinitionQuery().count();
            List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                    .latestVersion()
                    .listPage(param.getPageNumber() - 1, param.getPageSize());
            List<ActivitiModelDTO> list = processDefinitionList.stream().map((s) -> {
                ActivitiModelDTO modelDTO = new ActivitiModelDTO(s.getId(), s.getName(), s.getDescription(), s.getKey(), s.getVersion());
                return modelDTO;
            }).collect(Collectors.toList());
            PageInfo<ActivitiModelDTO> pageInfo = new PageInfo<>();
            pageInfo.setTotal(count);
            pageInfo.setList(list);
            logger.info("ACTIVITI_LOG[]getModelList[] 获取流程模板分页List");
            logger.info("getModelList 获取流程模板分页List,运行成功结束");
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getModelList param:{}  cause:{}", param, e.getMessage());
            return Reply.fail(ErrorConstant.ACTIVITI_SELECT_MODEL_BY_LIMIT_CODE_314001, ErrorConstant.ACTIVITI_SELECT_MODEL_BY_LIMIT_MSG_314001);
        }
    }

    @Override
    public Reply getActList(BaseParam param) {
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            List<Task> taskList = taskService.createTaskQuery()
                    .taskAssignee(loginName)
                    .taskName("管理员批准")
                    .listPage(param.getPageNumber() - 1, param.getPageSize());
            long count = taskService.createTaskQuery().taskAssignee(loginName).taskName("管理员批准").count();
            List<ActivitiActDTO> list = new ArrayList<>();
            for (Task t : taskList) {
                MwKnowledgeBaseTable mwKnowledgeBaseTable = mwKnowledgeService.selectByProcessId(t.getProcessInstanceId());
                t.setDescription(loginName + "知识提交申请" + t.getCreateTime());
                ActivitiActDTO actDTO = new ActivitiActDTO(t.getId(), t.getName(), t.getDelegationState(), t.getExecutionId(), t.getCreateTime());
                BeanUtils.copyProperties(mwKnowledgeBaseTable, actDTO);
                list.add(actDTO);
            }
            PageInfo<ActivitiActDTO> pageInfo = new PageInfo<>();
            pageInfo.setTotal(count);
            pageInfo.setList(list);
            logger.info("ACTIVITI_LOG[]getActList[] 获取我的待办分页List");
            logger.info("getActList 获取我的待办分页List,运行成功结束");
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getActList param:{}  cause:{}", param, e.getMessage());
            return Reply.fail(ErrorConstant.ACTIVITI_SELECT_ACT_BY_LIMIT_CODE_314002, ErrorConstant.ACTIVITI_SELECT_ACT_BY_LIMIT_MSG_314002);
        }
    }

    @Override
    public Reply batchDelByProcessIds(List<String> ids) {
        try {
            ids.forEach(e -> {
                try {
                    ActivitiUtils.deleteDeploymentByProcessId(e);
                } catch (Exception exception) {
                    log.error("batchDelByProcessIds", exception);
                }
            });

            logger.info("ACTIVITI_LOG[]batchDelByProcessIds[] 根据所选ids删除");
            logger.info("batchDelByProcessIds 根据所选ids删除,运行成功结束");
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to batchDelByProcessIds cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ACTIVITI_DELETE_BY_IDS_CODE_314003, ErrorConstant.ACTIVITI_DELETE_BY_IDS_MSG_314003);
        }
    }

    @Override
    public Reply batchDel(List<String> ids) {
        try {
            ids.forEach(e -> {
                repositoryService.deleteDeployment(e, true);
            });
            logger.info("ACTIVITI_LOG[]batchDel[] 根据所选ids删除");
            logger.info("batchDel 根据所选ids删除,运行成功结束");
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to batchDel cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ACTIVITI_DELETE_BY_IDS_CODE_314003, ErrorConstant.ACTIVITI_DELETE_BY_IDS_MSG_314003);
        }
    }

    @Override
    public Reply startWorkFlow(String key, Map<String, Object> variables) {
        Reply reply = null;
        try {
            //蛇皮流程id
            String processId = "";
            //判断何种审批
            switch (key) {
                //知识库审批
                case "Process_knowledge_plus":
                    reply = createProcessKnowledge(key, variables);
                    break;
                //ip地址分配审批
                case "ip_status":
                    reply = createProcessIP(key, variables);
                    break;
                default:
                    break;
            }
            return reply;
        } catch (Exception e) {
            log.error("fail to startWorkFlow cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ACTIVITI_CREATE_BY_KEY_CODE_314004, ErrorConstant.ACTIVITI_CREATE_BY_KEY_MSG_314004);
        }
    }

    @Override
    public Reply myApplyList(BaseParam param) {
        try {
            //创建查询对象，查询该用户发起的流程
            List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                    .startedBy(iLoginCacheInfo.getLoginName())
                    .listPage(param.getPageNumber() - 1, param.getPageSize());
            long count = historyService.createHistoricProcessInstanceQuery().startedBy(iLoginCacheInfo.getLoginName()).count();
            List<MyApplyDTO> applyDTOS = new ArrayList<>();
            list.forEach(s -> {
                MyApplyDTO applyDTO = new MyApplyDTO(s.getId(), s.getName(), s.getStartTime());
                ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(s.getId())
                        .singleResult();
                if (instance == null) {
                    applyDTO.setStatus(COMPLETED);
                } else {
                    applyDTO.setStatus(IMPLEMENTING);
                }
                applyDTOS.add(applyDTO);
            });
            PageInfo<MyApplyDTO> pageInfo = new PageInfo<>();
            pageInfo.setTotal(count);
            pageInfo.setList(applyDTOS);
            logger.info("ACTIVITI_LOG[]myApplyList[] 查看当前登录用户的申请列表");
            logger.info("myApplyList 查看当前登录用户的申请列表,运行成功结束");
            //返回流程实例的id
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to myApplyList cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ACTIVITI_SELECT_APPLY_BY_LIMIT_CODE_314005, ErrorConstant.ACTIVITI_SELECT_APPLY_BY_LIMIT_MSG_314005);
        }
    }

    @Override
    public Reply getApplyStatus(String instanceId) {
        try {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            //获取bpmnModel对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
            //因为我们这里只定义了一个process，所以获取集合中的第一个即可
            Process process = bpmnModel.getProcesses().get(0);
            //获取所有的FlowElement信息
            Collection<FlowElement> flowElements = process.getFlowElements();
            Map<String, String> map = new HashMap<>();
            for (FlowElement flowElement : flowElements) {
                //判断是否是连线
                if (flowElement instanceof SequenceFlow) {
                    SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                    String ref = sequenceFlow.getSourceRef();
                    String targetRef = sequenceFlow.getTargetRef();
                    map.put(ref + targetRef, sequenceFlow.getId());
                }
            }
            //获取流程实例 历史节点（全部）
            List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(instanceId).list();
            //各个历史节点  两两组合 key
            Set<String> keyList = new HashSet<>();
            for (HistoricActivityInstance i : list) {
                for (HistoricActivityInstance j : list) {
                    if (i != j) {
                        keyList.add(i.getActivityId() + j.getActivityId());
                    }
                }
            }
            //高亮连线ID
            Set<String> highLine = new HashSet<>();
            keyList.forEach(s -> highLine.add(map.get(s)));

            //获取流量实例 历史节点（已完成）
            List<HistoricActivityInstance> listFinished = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(instanceId).finished().list();
            //高亮节点ID
            Set<String> highPoint = new HashSet<>();
            listFinished.forEach(s -> highPoint.add(s.getActivityId()));

            //获取流量实例 历史节点（待办节点）
            List<HistoricActivityInstance> listUnFinished = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(instanceId).unfinished().list();
            //需要移除的高亮连线
            Set<String> set = new HashSet<>();
            //待办高亮节点
            Set<String> waitingToDo = new HashSet<>();
            listUnFinished.forEach(s -> {
                waitingToDo.add(s.getActivityId());
                for (FlowElement flowElement : flowElements) {
                    //判断是否是用户节点
                    if (flowElement instanceof UserTask) {
                        UserTask userTask = (UserTask) flowElement;
                        if (userTask.getId().equals(s.getActivityId())) {
                            List<SequenceFlow> outgoingFlows = userTask.getOutgoingFlows();
                            //因为 高亮连线查询的是所有节点 两两组合 把待办之后 往外发出的连线也包含进去了，所以要把高亮待办节点之后即出的连线去掉
                            if (outgoingFlows != null && outgoingFlows.size() > 0) {
                                outgoingFlows.forEach(a -> {
                                    if (a.getSourceRef().equals(s.getActivityId())) {
                                        set.add(a.getId());
                                    }
                                });
                            }
                        }
                    }
                }
            });
            highLine.removeAll(set);
            //获取当前用户
            String loginName = iLoginCacheInfo.getLoginName();
            //存放 高亮 办理节点
            Set<String> iDo = new HashSet<>();
            //当前用户已完成的任务
            List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery()
                    .taskAssignee(loginName)
                    .finished()
                    .processInstanceId(instanceId).list();
            taskInstances.forEach(a -> {
                iDo.add(a.getTaskDefinitionKey());
            });
            Map<String, Object> reMap = new HashMap<>();
            reMap.put("highPoint", highPoint);
            reMap.put("highLine", highLine);
            reMap.put("waitingToDo", waitingToDo);
            reMap.put("iDo", iDo);
            logger.info("ACTIVITI_LOG[]getApplyStatus[] 查看当前申请的流程图进度");
            logger.info("getApplyStatus 查看当前申请的流程图进度,运行成功结束");
            //返回流程实例的id
            return Reply.ok(reMap);
        } catch (Exception e) {
            log.error("fail to getApplyStatus cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ACTIVITI_SELECT_APPLY_VIEW_CODE_314006, ErrorConstant.ACTIVITI_SELECT_APPLY_VIEW_MSG_314006);
        }
    }

    @Override
    public Reply recallWorkFlow(String processId, String knowledgeId) {
        try {
            //查看任务
            Task task = ActivitiUtils.getTaskIdByProcessId(processId);
            String assignee = "";
            if (task != null && !"".equals(task)) {
                assignee = task.getAssignee();
            }
            //删除流程实例
            ActivitiUtils.deleteProcess(processId);
            //改变知识状态
            mwKnowledgeService.editorActivitiParam("", ACTIVITI_REVOCATION_STATUS, knowledgeId);
            //给下一个受理人发送消息
            if (iLoginCacheInfo.getCacheInfo(assignee) != null) { //说明下一步受理人在线
                Integer receiverUserId = iLoginCacheInfo.getCacheInfo(assignee).getUserId();
                countActList(receiverUserId, 0L);//给受理人发消息通知
            }
            logger.info("ACTIVITI_LOG[]recallWorkFlow[] 撤回知识的发布");
            logger.info("recallWorkFlow 撤回知识的发布,运行成功结束");
            //返回流程实例的id
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to recallWorkFlow cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ACTIVITI_DELETE_PROCESS_CODE_314007, ErrorConstant.ACTIVITI_DELETE_PROCESS_MSG_314007);
        }
    }

    @Override
    public Reply batchPass(OneClickParam param) throws Throwable {
        HashMap<String, Object> variables = new HashMap<>();
        Integer activitiStatus = 0;
        Integer audit = 0;
        if (param.getPassFlag()) {
            //审核通过
            audit = 1;// 网关条件参数
            activitiStatus = ACTIVITI_COMPLETED_STATUS;
        } else {
            //审核不通过
            audit = 0;// 网关条件参数
            variables.put(ProcessVariablesNameEnum.APPROVE.getName(), "");
            activitiStatus = ACTIVITI_REJECTED_STATUS;
        }
        if (param.getBatchPassParamList() != null && param.getBatchPassParamList().size() > 0) {
            for (BatchPassParam batchPassParam : param.getBatchPassParamList()) {
                taskService.setVariable(batchPassParam.getTaskId(), ProcessVariablesNameEnum.AUDIT.getName(), audit);
                if (!param.getPassFlag()) {
                    //被驳回需要添加驳回信息
                    taskService.addComment(batchPassParam.getTaskId(), batchPassParam.getProcessId(), param.getRejectedReason());
                }
                taskService.complete(batchPassParam.getTaskId(), variables);
                mwKnowledgeService.editorActivitiParam(batchPassParam.getProcessId(), activitiStatus, batchPassParam.getKnowledgeId());
                if (param.getPassFlag()) {
                    //审核通过创建知识索引,先根据知识id查出知识内容
                    MwKnowledgeBaseTable aParam = mwKnowledgeService.selectById(batchPassParam.getKnowledgeId());
                    //创建索引
                    LuceneFieldsDTO luceneFieldsDTO = new LuceneFieldsDTO(aParam.getId(), aParam.getTitle(), aParam.getTypeId().toString(), aParam.getTriggerCause(), aParam.getSolution());
                    try {
                        LuceneUtils.createIndex(luceneFieldsDTO);
                    } catch (Exception e) {
                        log.error("fail to LuceneUtils.createIndex(luceneFieldsDTO) cause:{}", e.getMessage());
                        throw new Throwable("创建索引失败！" + e);
                    }
                }
            }
            return Reply.ok();
        }
        return Reply.fail(ERRORINFO);
    }

    @Override
    public Reply countActList(Integer userId, Long passCount) {
        String key = WEBSOCKET_MESSAGE_COUNT + userId;
        String keyValue = redisTemplate.opsForValue().get(key);
        Long redisCount = 0L;
        if (keyValue != null && !"".equals(keyValue)) {
            redisCount = Long.parseLong(keyValue);
        }
        String loginName = "";
        MWUser userInfo = (MWUser) mwUserCommonService.selectByUserId(userId).getData();
        if (userInfo != null) {
            loginName = userInfo.getLoginName();
        }
        Long rejectedCount = taskService.createTaskQuery()
                .taskAssignee(loginName)
                .taskVariableValueEquals(ProcessVariablesNameEnum.AUDIT.getName(), 0).count();
        if (passCount == null) {
            passCount = 0L;
        }
        passCount = passCount + redisCount;
        redisTemplate.opsForValue().set(key, passCount.toString());
        Long auditCount = taskService.createTaskQuery()
                .taskAssignee(loginName)
                .taskVariableValueEquals(ProcessVariablesNameEnum.APPROVE.getName(), ADMIN)
                .taskVariableValueNotEquals(ProcessVariablesNameEnum.AUDIT.getName(), 0)
                .count();
        RealTimeUpdateDataDTO realTimeUpdateDataDTO = new RealTimeUpdateDataDTO(rejectedCount, passCount, auditCount);
        //初始化消息记录
        mwMessageService.createMessage(null, 2, 0, null);
//        if (webSocketGetCount.sessionPool.get(userId) != null) {
//            webSocketGetCount.sendObjMessage(userId, realTimeUpdateDataDTO);
//        }
        return Reply.ok(realTimeUpdateDataDTO);
    }

    @Override
    public Reply getRealTimeUpdateData(Integer userId, Long rejectedCount, Long auditCount, Long passCount) {
        if (rejectedCount == null) {
            rejectedCount = 0L;
        }
        if (auditCount == null) {
            auditCount = 0L;
        }
        if (passCount == null) {
            passCount = 0L;
        }
        String key = WEBSOCKET_MESSAGE_COUNT + userId;
        String keyValue = redisTemplate.opsForValue().get(key);
        Long redisCount = 0L;
        if (keyValue != null && !"".equals(keyValue)) {
            redisCount = Long.parseLong(keyValue);
        }
        String loginName = "";
        MWUser userInfo = (MWUser) mwUserCommonService.selectByUserId(userId).getData();
        if (userInfo != null) {
            loginName = userInfo.getLoginName();
        }
        rejectedCount = taskService.createTaskQuery()
                .taskAssignee(loginName)
                .taskVariableValueEquals(ProcessVariablesNameEnum.AUDIT.getName(), 0).count() + rejectedCount;

        passCount = passCount + redisCount;
        auditCount = taskService.createTaskQuery()
                .taskAssignee(loginName)
                .taskVariableValueEquals(ProcessVariablesNameEnum.APPROVE.getName(), ADMIN)
                .taskVariableValueNotEquals(ProcessVariablesNameEnum.AUDIT.getName(), 0)
                .count() + auditCount;
        RealTimeUpdateDataDTO realTimeUpdateDataDTO = new RealTimeUpdateDataDTO(rejectedCount, passCount, auditCount);
        //初始化消息记录
        mwMessageService.createMessage(null, 2, 0, null);
//        if (webSocketGetCount.sessionPool.get(userId) != null) {
//            webSocketGetCount.sendObjMessage(userId, realTimeUpdateDataDTO);
//        }
        return Reply.ok(realTimeUpdateDataDTO);
    }

    @Override
    public Reply deleteRedisPassCountByUserId(Integer userId) {
        String key = WEBSOCKET_MESSAGE_COUNT + userId;
        redisTemplate.delete(key);
        return Reply.ok();
    }

//    @Override
//    public Reply getTaskCommentsByProcessId(String processId) {
//        Map<String, List<Comment>> map = new HashMap();
//        //使用流程实例ID,查询历史任务，获取历史任务对应的每个任务
//        List<HistoricTaskInstance> taskInstanceList = historyService.createHistoricTaskInstanceQuery()
//                .processInstanceId(processId)
//                .list();
//        //遍历集合，获取每个任务id
//        if (taskInstanceList != null && taskInstanceList.size() > 0) {
//            for (HistoricTaskInstance historicTaskInstance : taskInstanceList) {
//                String taskId = historicTaskInstance.getId();
//                //获取批注信息
//                List<Comment> comments = taskService.getTaskComments(taskId);
//                if (comments != null && comments.size() > 0) {
//                    //当key值相同时不做覆盖而是在原基础上加
//                    if (map.get(historicTaskInstance.getAssignee()) != null) {
//                        comments.addAll(map.get(historicTaskInstance.getAssignee()));
//                    }
//                    //key值为批注人的姓名
//                    map.put(historicTaskInstance.getAssignee(), comments);
//                }
//            }
//        }
//        return Reply.ok(map);
//    }

    @Override
    public Reply getTaskCommentsByProcessId(String processId) {
        List<CommentsDTO> list = new ArrayList<>();
        //使用流程实例ID,查询历史任务，获取历史任务对应的每个任务
        List<HistoricTaskInstance> taskInstanceList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processId)
                .list();
        //遍历集合，获取每个任务id
        if (taskInstanceList != null && taskInstanceList.size() > 0) {
            for (HistoricTaskInstance historicTaskInstance : taskInstanceList) {
                String taskId = historicTaskInstance.getId();
                //获取批注信息
                List<Comment> comments = taskService.getTaskComments(taskId);
                if (comments != null && comments.size() > 0) {
                    comments.forEach(comment -> {
                        //将信息存到CommentsDTO中
                        CommentsDTO commentsDTO = new CommentsDTO(historicTaskInstance.getAssignee(), comment.getTime(), comment.getFullMessage());
                        list.add(commentsDTO);
                    });
                }
            }
        }
        return Reply.ok(list);
    }

    @Override
    public Reply getFlowViewData(String processId) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processId).singleResult();
        //获取bpmnModel对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
        FlowViewDataDTO flowViewData = ActivitiUtils.getFlowViewData(bpmnModel);
        //获取流程历史中已执行节点
        List<HistoricActivityInstance> historicActivityInstances = ActivitiUtils.getHistoricActivityInstance(processId);
        //流程所有节点信息
        List<FlowNodeDTO> nodes = flowViewData.getNodes();
        for (HistoricActivityInstance tempActivity : historicActivityInstances) {
            for (FlowNodeDTO node : nodes) {
                if (node.getId().equals(tempActivity.getActivityId())) {
                    node.setActive(true);
                }
            }
        }
        return Reply.ok(flowViewData);
    }


    //------------------------------function--------------------------------

    private Reply createProcessKnowledge(String key, Map<String, Object> variables) {
        //设置发起人
        variables.put(ProcessVariablesNameEnum.USER.getName(), iLoginCacheInfo.getLoginName());
        KnowledgeBaseParam knowledgeBaseParam = JSONObject.toJavaObject((JSON) variables.get(ProcessVariablesNameEnum.KNOWLEDGE_BASE_PARAM.getName()), KnowledgeBaseParam.class);
        String processId = knowledgeBaseParam.getProcessId();


        if (processId == null || "".equals(processId)) {
            processId = ActivitiUtils.startProcess(key, variables);
        }
        //添加知识库关联的参数
        mwKnowledgeService.editorActivitiParam(processId, ACTIVITI_RUNNING_STATUS, knowledgeBaseParam.getId());
        //查看流程
        String taskId = ActivitiUtils.queryKnowledgeProcessTaskId(iLoginCacheInfo.getLoginName(), processId);
        if (taskId != null && !"".equals(taskId)) {
            //给被驳回后的数据重新赋值
            taskService.setVariable(taskId, ProcessVariablesNameEnum.AUDIT.getName(), -1);
            //提交给管理员审核
            ActivitiUtils.completeKnowledgeTask(ADMIN, taskId);
        }
        logger.info("ACTIVITI_LOG[]startWorkFlow[] 发起申请，创建一个流程实例");
        logger.info("startWorkFlow 发起申请，创建一个流程实例,运行成功结束");
        return Reply.ok();
    }


    private Reply createProcessIP(String key, Map<String, Object> variables) {
        //设置发起人
        variables.put(ProcessVariablesNameEnum.USER.getName(), iLoginCacheInfo.getLoginName());
        KnowledgeBaseParam knowledgeBaseParam = JSONObject.toJavaObject((JSON) variables.get(ProcessVariablesNameEnum.KNOWLEDGE_BASE_PARAM.getName()), KnowledgeBaseParam.class);
        String processId = knowledgeBaseParam.getProcessId();

        if (processId == null || "".equals(processId)) {
            processId = ActivitiUtils.startProcess(key, variables);
        }
        //添加知识库关联的参数
        mwKnowledgeService.editorActivitiParam(processId, ACTIVITI_RUNNING_STATUS, knowledgeBaseParam.getId());
        //查看流程
        String taskId = ActivitiUtils.queryKnowledgeProcessTaskId(iLoginCacheInfo.getLoginName(), processId);
        if (taskId != null && !"".equals(taskId)) {
            //给被驳回后的数据重新赋值
            taskService.setVariable(taskId, ProcessVariablesNameEnum.AUDIT.getName(), -1);
            //提交给管理员审核
            ActivitiUtils.completeKnowledgeTask(ADMIN, taskId);
        }
        logger.info("ACTIVITI_LOG[]startWorkFlow[] 发起申请，创建一个流程实例");
        logger.info("startWorkFlow 发起申请，创建一个流程实例,运行成功结束");
        return Reply.ok();
    }

    @Override
    public Reply createProcess(ProcessParam processParam) throws Exception {

        if (null == processParam.getProcessDefinition() && StringUtils.isEmpty(processParam.getProcessId())) {
            log.error("createProcess", processParam);
            return Reply.fail("流程信息错误");
        }
        //判断现阶段流程是否与模型绑定
        //查看绑定模块操作
        Integer countBandOper = 1;
        //countBandOper = countBandOp(countBandOper, processParam.getProcessDefinition());
        ProcessNode processNode = processParam.getProcessDefinition().getChildNode().getChildNode();
        List<Integer> modeId = new ArrayList<>();
        for (List<Integer> integers : processNode.getNodeInfo().getModelId()) {
            modeId.add(integers.get(integers.size() - 1));
        }
        List<Integer> set = processNode.getNodeInfo().getOperType();
        //简称用户流程名称是否重复
        if (modeId.size() < 0 || countBandOper != 1 || !checkNodeName(processParam.getProcessDefinition().getChildNode())) {
            return Reply.fail("流程没有与模型绑定或者流程存在绑定多个拦截操作，或节点长度<=1");
        }
       /* if (processNode.getNodeInfo().getInfoType() != 2) {
            return Reply.fail("首节点未绑定模型");
        }*/
        List<String> String = processDao.selectProcessId(processParam.getProcessId(), modeId, set);
/*        if (StringUtils.isNotEmpty(processParam.getProcessId())) {
            String.remove(processParam.getProcessId());
        }*/
        if (String.size() > 0) {
            return Reply.fail("当前操作节点内存在模型id操作被流程:" + String.toString() + "绑定");
        }


        BpmnModel bpmnModel = null;
        if (StringUtils.isNotEmpty(processParam.getProcessId())) {
            //查询流程信息
            bpmnModel = BpmnModelUtils.createProcess(processParam);
        } else {
            bpmnModel = BpmnModelUtils.createProcess(processParam);
        }

        DeploymentBuilder builder = repositoryService.createDeployment();
        String processName = processParam.getProcessName() + ".bpmn";
        Deployment deploy = builder.name(processName)//申明流程名称
                .addBpmnModel(processName, bpmnModel)
                .deploy();//完成部署

        String activitiId = bpmnModel.getMainProcess().getId();

        String defId = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult().getId();

        ProcessDefDTO processDTO = new ProcessDefDTO();
        String processData = JSONObject.toJSONString(processParam);
        processDTO.setProcessData(processData);
        processDTO.setProcessInstanceKey(activitiId);
        //流程与模块绑定
        if (StringUtils.isEmpty(processParam.getProcessId())) {
            updateProcessAndMoudle("noProcess", modeId, set, defId, activitiId);
        } else {
            updateProcessAndMoudle(processParam.getProcessId(), modeId, set, defId, activitiId);
        }
        processDTO.setVersion(deploy.getVersion());
        if (StringUtils.isEmpty(processParam.getProcessId())) {
            //新增流程定义
            processDTO.setProcessDefinitionId(defId);
            processDao.insertProcessDef(processDTO);
            processParam.setProcessId(defId);
        } else {
            //更新流程定义
            processDTO.setProcessDefinitionId(processParam.getProcessId());
            processDTO.setNewProcessDefinitionId(defId);
         /*   long count = runtimeService.createProcessInstanceQuery()
                    .processDefinitionId(processDTO.getProcessDefinitionId()).count();
            if (count == 0) {
                ActivitiUtils.deleteDeploymentByProcessId(processDTO.getProcessDefinitionId());
            }*/
            processDao.deleteProcessDef(2, processParam.getProcessId());
            processDTO.setProcessDefinitionId(defId);
            processDao.insertProcessDef(processDTO);
            /*updateProcess(processDTO);*/
           /* DeleteDto deleteDto = DeleteDto.builder()
                    .type(DataType.PROCESS.getName())
                    .typeId(processParam.getProcessId()).build();
            mwCommonService.deleteMapperAndPerm(deleteDto);*/
        }
        //新增数据权限信息
        processParam.setProcessId(defId);
        updateMapperAndPerms(processParam);
        return Reply.ok(processDTO);
    }

    private boolean checkNodeName(ProcessNode childNode) {
        List<String> name = new ArrayList<>();
        if (checkNodeNameCount(name, childNode)) {
            return false;
        }
        return true;
    }

    private boolean checkNodeNameCount(List<String> name, ProcessNode childNode) {
        if (name.contains(childNode.getNodeName())) {
            return true;
        } else {
            if (childNode.getChildNode() != null) {
                return name.contains(childNode.getNodeName());
            }
            return false;
        }
    }

    private void updateProcessAndMoudle(String noProcess, List<Integer> modeId, List<Integer> set, String defId, String id) {
        if (!noProcess.equals("noProcess")) {
            processDao.deleteProcessAndMoudle(2, noProcess);
        }
        processDao.insertProcessAndMoudle(modeId, set, defId, id);
    }


    private List<Integer> getSettype(int settype) {
        switch (settype) {
            case 0:
                return Arrays.asList(0);
            case 1:
                return Arrays.asList(1);
            case 2:
                return Arrays.asList(2);
            default:
                return Arrays.asList(0, 1, 2);
        }
    }

    private Integer countBandOp(Integer i, ProcessNode processDefinition) {
        if (null != processDefinition.getChildNode()) {
            if (processDefinition.getNodeInfo().getInfoType() == 2) {
                i = i + 1;
            }
            i = countBandOp(i, processDefinition.getChildNode());
        }
        return i;
    }


   /* private void updateProcess(ProcessDefDTO processDTO) throws Exception {
        //判断流程是否存在实例
        long count = runtimeService.createProcessInstanceQuery()
                .processDefinitionId(processDTO.getProcessDefinitionId()).count();

        if (count == 0) {
            ActivitiUtils.deleteDeploymentByProcessId(processDTO.getProcessDefinitionId());
        }

        processDao.updateProcessDef(processDTO);

        Map map = new HashMap();
        map.put("processDefinitionId", processDTO.getProcessDefinitionId());
        ProcessModuleBindDTO processModuleBindDTO = processDao.getProcessModuleBindDTO(map);

        if (null != processModuleBindDTO) {
            processModuleBindDTO.setProcessDefinitionId(processDTO.getNewProcessDefinitionId());
            processModuleBindDTO.setAction(null);
            processModuleBindDTO.setModelName(null);
            processDao.updateProcessModuleBindDTO(processModuleBindDTO);
        }
    }*/

    @Override
    public Reply searchProcess(SearchProcessParam processParam) throws Exception {
        List<ProcessDefinition> list = new ArrayList<>();
        Integer sreachtype = 2; //查询所有未删除流程定义
        if (processParam.getProcessDefinitionId() != null) {
            sreachtype = 3; //查询所有流程
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processParam.getProcessDefinitionId())
                    .singleResult();
            list.add(processDefinition);
        } else {
            list = repositoryService
                    .createProcessDefinitionQuery().latestVersion()
                    .list();
        }
        List<ProcessDTO> processDTOS = new ArrayList<>();
        Map<String, ProcessDTO> idMap = new HashMap<>();
        for (ProcessDefinition processDefinition : list) {
            try {
                if (!processDefinition.getName().equals("知识提交流程")) {
                    ProcessDTO processDTO = ProcessDTO.builder()
                            .processName(processDefinition.getName())
                            .processDefinitionId(processDefinition.getId())
                            .build();
                    processDTOS.add(processDTO);
                    idMap.put(processDTO.getProcessDefinitionId(), processDTO);
                }

            } catch (Exception e) {
                /*ProcessDTO processDTO = null;
                processDTO = ProcessDTO.builder()
                        .processDefinitionId(processDefinition.getId())
                        .build();
                processDTOS.add(processDTO);
                idMap.put(processDTO.getProcessDefinitionId(), processDTO);*/
            }
        }
        //返回数据权限数据, 及流程定义信息
        List<String> ids = new ArrayList<>(idMap.keySet());
        //判断知识库流程是否存在。此处双返回
        if (ids.size() == 0) {
            PageInfo pageInfo = new PageInfo<>();
            return Reply.ok(pageInfo);
        } else {
            List<ProcessDefDTO> listProcessDef = listProcessDef(ids, sreachtype);
            List<ProcessDTO> resprocessDTOS = new ArrayList<>();

            listProcessDef.forEach(data -> {
                ProcessDTO dto = idMap.get(data.getProcessDefinitionId());
                if (null != dto) {
                    dto.setUser(data.getUserIds());
                    dto.setOrg(data.getOrganizes());
                    dto.setGroup(data.getGroupIds());
                    if (data.getOrganizes() != null && data.getOrganizes().toString().trim().equals("")) {
                        dto.setOrgText(mwOrgCommonService.getOrgnamesByids(data.getOrganizes()));
                    }
                    if (data.getGroupIds() != null && data.getGroupIds().toString().trim().equals("")) {
                        dto.setGroupText(mwUserGroupCommonService.getGroupnamesByids(data.getGroupIds()));
                    }
                    if (data.getUserIds() != null && data.getUserIds().toString().trim().equals("")) {
                        dto.setUserText(mwUserCommonService.getLoginNameByUserIds(data.getUserIds()));
                    }

                    dto.setProcessStatus(data.getStatus() == 0);
                    try {
                        ProcessParam param = JSONObject.parseObject(data.getProcessData(), ProcessParam.class);
                        dto.setProcessName(param.getProcessName());
                        ProcessParamView processParamView = new ProcessParamView();
                        BeansUtils.copyProperties(param, processParamView);
                        processParamView.setChildNode(param.getProcessDefinition());
                        if (processParam.getProcessInstanceId() != null) {
                            int oper = processDao.selectOper(processParam.getProcessInstanceId());
                            dto.setAction(oper);
                            List<NodeProcess> nodeProcesses = new ArrayList<>();
                            ProcessNode p = processParamView.getChildNode().getChildNode();
                            List<HistoricTaskInstance> taskInstances = processEngine.getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(processParam.getProcessInstanceId()).includeProcessVariables().orderByHistoricTaskInstanceEndTime().desc().list();
                            int i = 0;
                            int submitAgree = 1;
                            do {
                                NodeProcess process = new NodeProcess();
                                process.setNodeName(p.getNodeName());
                                process.setNodeType(p.getNodeInfo().getInfoType());
                                for (HistoricTaskInstance historicTaskInstance : taskInstances) {
                                    if (historicTaskInstance.getName().equals(p.getNodeName())) {
                                        process.setAssign(historicTaskInstance.getAssignee());
                                        Integer accept = 2;
                                        process.setAccpet(accept);
                                        try {
                                            Map<String, Object> objects = historicTaskInstance.getProcessVariables();
                                            for (String s : objects.keySet()) {
                                                if (s.equals(historicTaskInstance.getId())) {
                                                    Integer assign = (Integer) objects.get(s);
                                                    process.setAccpet(assign == 0 ? 1 : 0);
                                                }
                                                if (s.equals("data")) {
                                                    ActivitiParam activitiParam = JSONObject.parseObject(objects.get(s).toString(), ActivitiParam.class);
                                                    if (i == 0) {
                                                        i++;
                                                        process.setType(0);
                                                        process.setData(activitiParam.getData());
                                                    } else {
                                                        List<Comment> comments = taskService.getTaskComments(historicTaskInstance.getId());
                                                        List<String> strings = new ArrayList<>();
                                                        for (Comment v : comments) {
                                                            strings.add(v.getFullMessage());
                                                        }
                                                        process.setType(1);
                                                        process.setData(strings);
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                        }
                                        process.setStartTime(historicTaskInstance.getStartTime());
                                        process.setEndTime(historicTaskInstance.getEndTime());
                                    }
                                }
                                nodeProcesses.add(process);
                                p = p.getChildNode();
                                i++;
                            } while (p != null);
                            processParamView.setNodeProcesses(nodeProcesses);
                        }
                        dto.setProcessData(processParamView);
                        resprocessDTOS.add(dto);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                }
            });
   /*         PageInfo pageInfo = new PageInfo<>(resprocessDTOS);
            pageInfo.setList(resprocessDTOS);*/
            PageList pageList = new PageList();
            List newList = pageList.getList(resprocessDTOS, processParam.getPageNumber(), processParam.getPageSize());
            PageInfo pageInfo = new PageInfo<>(resprocessDTOS);
            pageInfo.setPages(pageList.getPages());
            pageInfo.setPageNum(processParam.getPageNumber());
            pageInfo.setEndRow(pageList.getEndRow());
            pageInfo.setStartRow(pageList.getStartRow());
            pageInfo.setList(newList);
            return Reply.ok(pageInfo);
        }
    }

    private ProcessView getProcessDef(String processDefinitionId) {
        Map map = new HashMap();
        map.put("processDefinitionId", processDefinitionId);
        ProcessDefDTO processDefDTO = processDao.getProcessDefDTO(map);

        ProcessView processView = new ProcessView();
        processView.setProcessData(processDefDTO.getProcessData());
        return processView;
    }

    private List<ProcessDefDTO> listProcessDef(List<String> processDefinitionIds, Integer sreachtype) {
        Map map = new HashMap();
        map.put("ids", processDefinitionIds);
        List<ProcessDefDTO> processDefDTO = processDao.listProcessDefDTO(map, sreachtype);

        return processDefDTO;
    }

    @Override
    public String startProcess(String processId) {
        return startProcess(processId, null);
    }

    @Override
    public void completeTask(String taskId, String processId) {
        HistoricTaskInstance his = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId).singleResult();

        Task task = taskService.createTaskQuery().processInstanceId(processId).active().singleResult();
        ;
        log.info(task.getName());
        taskService.complete(task.getId());
        task = taskService.createTaskQuery().processInstanceId(processId).active().singleResult();

        his = historyService.createHistoricTaskInstanceQuery().processInstanceId(processId).singleResult();
        log.info(his.toString());
    }

    @Override
    public void deleteProcessById(DeleteParam deleteParam) {
        for (String s : deleteParam.getProcessIds()) {
            processDao.deleteProcessAndMoudle(2, s);
            processDao.deleteProcessDef(2, s);
        }
    }

    @Override
    public Reply getTask(TaskEntiy taskEntiy) {
        List<TaskEntiy> taskEntiys = new ArrayList<>();
        List<ProcessDefinition> list = repositoryService
                .createProcessDefinitionQuery().list();
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        Integer mwRole = mwRoleService.selectByUserId(userId).getRoleId();
        Map<String, Map<String, List<String>>> map = new HashMap<>();
        Set<String> strings = new HashSet<>();
        for (ProcessDefinition p : list) {
            Map<String, List<String>> userPremsion = checkUser(p.getId());
            if (checkUsePremsion(userPremsion, userId, groupIds, mwRole, null)) {
                strings.add(p.getId());
                map.put(p.getId(), userPremsion);
            }
        }
        if (strings.size() > 0) {
            List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionIds(strings).list();
            for (ProcessInstance processInstance : processInstances) {


                List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
                for (Task task : tasks) {
                    Map<String, List<String>> maps = map.get(processInstance.getProcessDefinitionId());
                    if (checkUsePremsion(maps, userId, groupIds, mwRole, task.getName())) {
                        List<HistoricTaskInstance> taskInstances = processEngine.getHistoryService() // 历史相关Service
                                .createHistoricTaskInstanceQuery() // 创建历史活动实例查询
                                .processInstanceId(processInstance.getId())
                                .includeProcessVariables()
                                .finished().orderByHistoricTaskInstanceEndTime().desc()
                                .list();
                        TaskEntiy taskEntiyn = new TaskEntiy();
                        taskEntiyn.setAcctivitiName(processInstance.getProcessDefinitionName());
                        if (taskInstances.size() > 0) {
                            taskEntiyn.setBeforTaskName(taskInstances.get(0).getName());
                            taskEntiyn.setBeforeSubmitUser(taskInstances.get(0).getAssignee());
                            taskEntiyn.setBeforTaskEndTime(taskInstances.get(0).getEndTime());
                        }
                        taskEntiyn.setProcessDefinitionId(processInstance.getProcessDefinitionId());
                        taskEntiyn.setProcessInstanceId(processInstance.getId());
                        taskEntiyn.setSubmitTime(processInstance.getStartTime());

                        Map<String, Object> kap = new HashMap<>();
                        try {
                            kap = taskInstances.get(0).getProcessVariables();
                        } catch (Exception e) {

                        }


                        String s = "cut" + task.getTaskDefinitionKey().split("cut")[1] + "cut";
                        List<Map<String, Object>> mapList = processDao.selectByTaskId(task.getProcessDefinitionId(), s);
                        Integer moudleId = 0;
                        String moudleName = "";
                        Integer modelId = 0;

                        if (mapList.size() > 0) {
                            modelId = Integer.parseInt(mapList.get(0).get("model_id").toString());
                        }

                        try {
                            moudleId = (Integer) kap.get("modelId");
                            moudleName = getModleName(moudleId);
                        } catch (Exception e) {

                        }
                        List<Integer> ids = new ArrayList<>();
                        for (String l : kap.keySet()) {
                            if (l.contains("model_instance_id")) {
                                ids.add(Integer.parseInt(kap.get(l).toString()));
                            }
                        }
                        String name = "该审批出现问题";
                        try {
                            name = mwModelViewCommonService.getNameListByIds(ids).get(0).getInstanceName();
                        } catch (Exception e) {

                        }

                        taskEntiyn.setMoudleName(name);
                        taskEntiyn.setMoudleId(moudleId);
                        taskEntiyn.setModelId(modelId);
                   /* String name = kap.get("submitBandModelName").toString();
                    String submitBandUser = kap.get("submitBandUser").toString();
                    String acctivitiName = kap.get("acctivitiName").toString();
                    Date submitTime = (Date) kap.get("submitTime");*/
                        taskEntiyn.setTaskName(task.getName());
                    /*taskEntiyn.setSubmitBandModelName(name);
                    taskEntiyn.setSubmitBandUser(submitBandUser);*/
                        if (checkVariable(maps.get(task.getName()))) {
                            if (kap.get("USER_VARIABLE") != null) {
                                if (kap.get("USER_VARIABLE").toString().equals(userId.toString())) {
                                    taskEntiys.add(taskEntiyn);
                                }
                            } else {
                                taskEntiys.add(taskEntiyn);
                            }
                        } else {
                            taskEntiys.add(taskEntiyn);
                        }
                    }
                }
                /*Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().singleResult();*/
            }
        }


        if (taskEntiy.getAcctivitiName() != null) {
            taskEntiys = taskEntiys.stream().filter(data -> data.getAcctivitiName().contains(taskEntiy.getAcctivitiName())).collect(Collectors.toList());
        }
        if (taskEntiy.getSubmitTimeStart() != null) {
            taskEntiys = taskEntiys.stream().filter(data -> data.getSubmitTime().after(taskEntiy.getSubmitTimeStart()) && data.getSubmitTime().before(taskEntiy.getSubmitTimeEnd())).collect(Collectors.toList());
        }
        if (taskEntiy.getAcctivitiName() != null) {
            taskEntiys = taskEntiys.stream().filter(data -> data.getAcctivitiName().contains(taskEntiy.getAcctivitiName())).collect(Collectors.toList());
        }

        Collections.sort(taskEntiys, new Comparator<TaskEntiy>() {
            @Override
            public int compare(TaskEntiy o1, TaskEntiy o2) {
                return o2.getBeforTaskEndTime().compareTo(o1.getBeforTaskEndTime());
            }
        });

        List<String> userLoginName = new ArrayList<>();
        userLoginName.add(loginName);
        insertOrUpdateCount(userLoginName, taskEntiys.size(), 1);
        PageList pageList = new PageList();
        int pagenaum = (int) Math.ceil((double) taskEntiys.size() / (double) taskEntiy.getPageSize());
        int[] pageNum = new int[pagenaum];
        for (int i = 0; i < pagenaum; i++) {
            pageNum[i] = i + 1;
        }


        PageHelper.startPage(taskEntiy.getPageNumber(), taskEntiy.getPageSize());
        PageInfo pageInfo = new PageInfo<>(pageList.getList(taskEntiys, taskEntiy.getPageNumber(), taskEntiy.getPageSize()));
        pageInfo.setList(pageList.getList(taskEntiys, taskEntiy.getPageNumber(), taskEntiy.getPageSize()));
        pageInfo.setHasNextPage(taskEntiys.size() > taskEntiy.getPageNumber() * taskEntiy.getPageSize());
        pageInfo.setNavigatepageNums(pageNum);
        pageInfo.setIsLastPage(taskEntiys.size() <= taskEntiy.getPageNumber() * taskEntiy.getPageSize());
        pageInfo.setTotal(taskEntiys.size());
        pageInfo.setSize(taskEntiys.size());

        return Reply.ok(pageInfo);
    }

    private String getModleName(Integer moudleId) {
        String moudleName = "";
        switch (moudleId) {
            case 1:
                moudleName = "数据下发管理";
                break;
            case 2:
                moudleName = "事件管理";
                break;
            case 3:
                moudleName = "问题管理";
                break;
            case 4:
                moudleName = "变更管理";
                break;
            case 5:
                moudleName = "发布管理";
                break;
            case 6:
                moudleName = "任务管理";
                break;
            default:
                moudleName = "";
                break;
        }
        return moudleName;
    }

    @Override
    public void activite(ActivitiActParam activitiActParam) {
        Integer oper = 1;
        if (activitiActParam.isActivitiSign()) {
            oper = 0;
        }
        processDao.deleteProcessAndMoudle(oper, activitiActParam.getProcessIds());
        processDao.deleteProcessDef(oper, activitiActParam.getProcessIds());
    }

    @Override
    public Reply getMyTask(MyTaskEntiy taskEntiy) {
        //获取用户身份
        String loginName = iLoginCacheInfo.getLoginName();
        List<String> processInstanceId = processDao.selectTaskProcessId(loginName);
        HistoricTaskInstanceQuery historicTaskInstances = historyService // 历史相关Service
                .createHistoricTaskInstanceQuery(); // 创建历史活动实例查询
        List<HistoricTaskInstance> historicTaskInstanceList = new ArrayList<>();
        if (processInstanceId.size() == 0) {
            return null;
        }
        if (taskEntiy.getType() == 0) {
            historicTaskInstanceList = historyService // 历史相关Service
                    .createHistoricTaskInstanceQuery() // 历史相关Service// 创建历史活动实例查询
                    .taskAssignee(loginName)
                    .includeProcessVariables()
                    .processInstanceIdIn(processInstanceId)
                    .processFinished()
                    .processVariableValueEquals("accept", true)
                    .list();
        } else if (taskEntiy.getType() == 2) {
            historicTaskInstanceList = historyService // 历史相关Service
                    .createHistoricTaskInstanceQuery() // 历史相关Service// 创建历史活动实例查询
                    .taskAssignee(loginName)
                    .includeProcessVariables()
                    .processInstanceIdIn(processInstanceId)
                    .processVariableValueEquals("accept", true)
                    .processUnfinished().list();
        } else if (taskEntiy.getType() == 1) {
            historicTaskInstanceList = historyService // 历史相关Service
                    .createHistoricTaskInstanceQuery() // 历史相关Service// 创建历史活动实例查询
                    .taskAssignee(loginName)
                    .includeProcessVariables()
                    .processInstanceIdIn(processInstanceId)
                    .processVariableValueEquals("accept", false)
                    .processFinished().list();
        } else {
            historicTaskInstanceList = historyService // 历史相关Service
                    .createHistoricTaskInstanceQuery() // 历史相关Service// 创建历史活动实例查询
                    .taskAssignee(loginName)
                    .processInstanceIdIn(processInstanceId)
                    .includeTaskLocalVariables()
                    .list();
        }
        List<MyTaskEntiy> myTaskEntiys = new ArrayList<>();
        for (HistoricTaskInstance h : historicTaskInstanceList) {
            Map<String, Object> map = h.getProcessVariables();
            MyTaskEntiy m = new MyTaskEntiy();
            m.setProcessDefinitionId(h.getProcessDefinitionId());
            m.setProcessInstanceId(h.getProcessInstanceId());
            m.setProcessEndTime(h.getEndTime());
            m.setType(taskEntiy.getType());
            m.setProcessName(h.getName());
            m.setSubmitTime(h.getStartTime());
            Integer moudleId = 0;
            String moudleName = "";
            Integer modelId = 0;
            try {
                moudleId = (Integer) map.get("modelId");
                moudleName = getModleName(moudleId);
            } catch (Exception e) {

            }
            List<Integer> ids = new ArrayList<>();
            String processName = "";
            for (String l : map.keySet()) {
                if (l.contains("model_instance_id")) {
                    ids.add(Integer.parseInt(map.get(l).toString()));
                }
                if (l.equals("processName")) {
                    processName = map.get(l).toString();
                }
            }
            String name = mwModelViewCommonService.getNameListByIds(ids).get(0).getInstanceName();
            m.setMoudleName(name);
            m.setMoudleId(moudleId);
            m.setModelId(modelId);
            m.setTaskName(h.getName());
            m.setProcessName(processName);
            myTaskEntiys.add(m);
//            m.setAcctivitiName(h.getp);
        }
        if (taskEntiy.getProcessName() != null) {
            myTaskEntiys = myTaskEntiys.stream().filter(data -> data.getProcessName().contains(taskEntiy.getProcessName())).collect(Collectors.toList());
        }
        if (taskEntiy.getSubmitTimeStart() != null) {
            myTaskEntiys = myTaskEntiys.stream().filter(data -> data.getSubmitTime().after(taskEntiy.getSubmitTimeStart()) && data.getSubmitTime().before(taskEntiy.getSubmitTimeEnd())).collect(Collectors.toList());
        }
        if (taskEntiy.getSubmitBandModelName() != null) {
            myTaskEntiys = myTaskEntiys.stream().filter(data -> data.getSubmitBandModelName().contains(taskEntiy.getSubmitBandModelName())).collect(Collectors.toList());
        }

        Collections.sort(myTaskEntiys, new Comparator<MyTaskEntiy>() {
            @Override
            public int compare(MyTaskEntiy o1, MyTaskEntiy o2) {

                return o2.getProcessEndTime().compareTo(o1.getProcessEndTime());
            }
        });

        PageList pageList = new PageList();
        int pagenaum = (int) Math.ceil((double) myTaskEntiys.size() / (double) taskEntiy.getPageSize() == 0 ? 20 : taskEntiy.getPageSize());
        int[] pageNum = new int[pagenaum];
        for (int i = 0; i < pagenaum; i++) {
            pageNum[i] = i + 1;
        }

        PageHelper.startPage(taskEntiy.getPageNumber(), taskEntiy.getPageSize());
        PageInfo pageInfo = new PageInfo<>(pageList.getList(myTaskEntiys, taskEntiy.getPageNumber(), taskEntiy.getPageSize()));
        pageInfo.setList(pageList.getList(myTaskEntiys, taskEntiy.getPageNumber(), taskEntiy.getPageSize()));
        pageInfo.setHasNextPage(myTaskEntiys.size() > taskEntiy.getPageNumber() * taskEntiy.getPageSize());
        pageInfo.setNavigatepageNums(pageNum);
        pageInfo.setIsLastPage(myTaskEntiys.size() <= taskEntiy.getPageNumber() * taskEntiy.getPageSize());
        pageInfo.setTotal(myTaskEntiys.size());
        pageInfo.setSize(myTaskEntiys.size());

        return Reply.ok(pageInfo);
    }

    @Override
    public Reply getProcess(MyProcess myProcess) {
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        Integer mwRole = mwRoleService.selectByUserId(userId).getRoleId();
        List<ProcessDefinition> list = repositoryService
                .createProcessDefinitionQuery().list();
        List<String> strings = new ArrayList<>();
        for (ProcessDefinition p : list) {
            Map<String, List<String>> userPremsion = checkUser(p.getId());
            if (checkUsePremsion(userPremsion, userId, groupIds, mwRole, null)) {
                strings.add(p.getKey());
            }
        }
        // 创建历史活动实例查询
        List<HistoricTaskInstance> historicTaskInstanceList = new ArrayList<>();
        if (myProcess.getType() == 0) {
            historicTaskInstanceList = historyService// 历史相关Service
                    .createHistoricTaskInstanceQuery() // 历史相关Service// 创建历史活动实例查询
                    .taskAssignee(loginName)
                    .processDefinitionKeyIn(strings)
                    .includeProcessVariables()
                    .processFinished()
                    .processVariableValueEquals("accept", true)
                    .list();
        } else if (myProcess.getType() == 1) {
            historicTaskInstanceList = historyService// 历史相关Service
                    .createHistoricTaskInstanceQuery() // 历史相关Service// 创建历史活动实例查询
                    .taskAssignee(loginName)
                    .processDefinitionKeyIn(strings)
                    .includeProcessVariables()
                    .processVariableValueEquals("accept", false)
                    .processFinished().list();
        } else if (myProcess.getType() == 2) {
            historicTaskInstanceList = historyService// 历史相关Service
                    .createHistoricTaskInstanceQuery() // 历史相关Service// 创建历史活动实例查询
                    .taskAssignee(loginName)
                    .processDefinitionKeyIn(strings)
                    .includeProcessVariables()
                    .processVariableValueEquals("accept", true)
                    .processUnfinished().list();
        }


        List<MyProcess> myProcesses = new ArrayList<>();
        for (HistoricTaskInstance h : historicTaskInstanceList) {
            MyProcess m = new MyProcess();
            Map<String, Object> map = h.getProcessVariables();
            m.setProcessDefinitionId(h.getProcessDefinitionId());
            m.setProcessInstanceId(h.getProcessInstanceId());
            m.setProcessEndTime(h.getEndTime());
            m.setType(myProcess.getType());
            m.setProcessName(h.getName());
            m.setSubmitTime(h.getStartTime());
            Integer moudleId = 0;
            String moudleName = "";
            Integer modelId = 0;
            try {
                moudleId = (Integer) map.get("modelId");
                moudleName = getModleName(moudleId);
            } catch (Exception e) {

            }
            List<Integer> ids = new ArrayList<>();
            String processName = "";
            for (String l : map.keySet()) {
                if (l.contains("model_instance_id")) {
                    ids.add(Integer.parseInt(map.get(l).toString()));
                }
                if (l.equals("processName")) {
                    processName = map.get(l).toString();
                }
            }
            String name = mwModelViewCommonService.getNameListByIds(ids).get(0).getInstanceName();
            m.setMoudleName(name);
            m.setMoudleId(moudleId);
            m.setModelId(modelId);
            m.setProcessName(processName);
            m.setTaskName(h.getName());
            myProcesses.add(m);
//            m.setAcctivitiName(h.getp);
        }
        if (myProcess.getProcessName() != null) {
            myProcesses = myProcesses.stream().filter(data -> data.getProcessName().contains(myProcess.getProcessName())).collect(Collectors.toList());
        }
        if (myProcess.getSubmitTimeStart() != null) {
            myProcesses = myProcesses.stream().filter(data -> data.getSubmitTime().after(myProcess.getSubmitTimeStart()) && data.getSubmitTime().before(myProcess.getSubmitTimeEnd())).collect(Collectors.toList());
        }
        if (myProcess.getSubmitBandModelName() != null) {
            myProcesses = myProcesses.stream().filter(data -> data.getSubmitBandModelName().contains(myProcess.getSubmitBandModelName())).collect(Collectors.toList());
        }

        Collections.sort(myProcesses, new Comparator<MyProcess>() {
            @Override
            public int compare(MyProcess o1, MyProcess o2) {
                return o2.getProcessEndTime().compareTo(o1.getProcessEndTime());
            }
        });

        PageList pageList = new PageList();
        int pagenaum = (int) Math.ceil((double) myProcesses.size() / (double) myProcess.getPageSize() == 0 ? 20 : myProcess.getPageSize());
        int[] pageNum = new int[pagenaum];
        for (int i = 0; i < pagenaum; i++) {
            pageNum[i] = i + 1;
        }

        PageHelper.startPage(myProcess.getPageNumber(), myProcess.getPageSize());
        PageInfo pageInfo = new PageInfo<>(pageList.getList(myProcesses, myProcess.getPageNumber(), myProcess.getPageSize()));
        pageInfo.setList(pageList.getList(myProcesses, myProcess.getPageNumber(), myProcess.getPageSize()));
        pageInfo.setHasNextPage(myProcesses.size() > myProcess.getPageNumber() * myProcess.getPageSize());
        pageInfo.setNavigatepageNums(pageNum);
        pageInfo.setIsLastPage(myProcesses.size() <= myProcess.getPageNumber() * myProcess.getPageSize());
        pageInfo.setTotal(myProcesses.size());
        pageInfo.setSize(myProcesses.size());

        return Reply.ok(pageInfo);
    }

    @Override
    public Reply complete(MyProcess myProcess) {
        Map<String, Object> map = new HashMap<>();
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        Integer mwRole = mwRoleService.selectByUserId(userId).getRoleId();
        Integer User_Variable = 0;
        String mouldId = "0";
        List<String> processInstanceIds = new ArrayList<>();
        if (myProcess.getCompeleteType() == 0) {
            processInstanceIds.add(myProcess.getProcessInstanceId());
        } else {
            processInstanceIds = myProcess.getProcessInstanceIds();
        }
        for (String b : processInstanceIds) {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(b).includeProcessVariables().list();
            for (Task task : tasks) {
                if (task.getName().equals(myProcess.getTaskName())) {
                    Map<String, Object> kap = task.getProcessVariables();
                    String name = task.getName();
                    if (kap.get("startName") != null) {
                        name = kap.get("startName").toString();
                    }
                    if (kap.get("USER_VARIABLE") != null) {
                        User_Variable = (Integer) kap.get("USER_VARIABLE");
                    }
                    mouldId = kap.get("modelId").toString();

                    Map<String, List<String>> userPremsion = checkUser(task.getProcessDefinitionId());
                    if (checkUsePremsion(userPremsion, userId, groupIds, mwRole, task.getName()) && myProcess.getType() == 1) {
                        processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), "accept", true);
                        processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), task.getId() + "comment", myProcess.getComment());
                        taskService.setVariable(task.getId(), "type", 0);
                        taskService.setVariable(task.getId(), task.getId(), 0);
                        taskService.addComment(task.getId(), task.getProcessInstanceId(), myProcess.getComment());
                        taskService.claim(task.getId(), loginName);
                        taskService.complete(task.getId());


                        List<MWUser> mwUsers = processDao.selectByDefintId(task.getTaskDefinitionKey());

                        if (mwUsers.size() > 0) {
                            mwMessageService.sendFailAlertMessage(name + "已提交，请知悉", mwUsers, name, false, null);
                        }
                        List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(b).includeProcessVariables().list();
                        if (nextTasks.size() == 0) {
                            //判断 审批已经完成 将表单状态完成
                            UpdateTaskList(myProcess.getProcessInstanceId(), 0);
                        }
                        if (!mouldId.equals("2")) {
                            //完成oa节点代办
                            SendOaMessage(name, loginName, myProcess.getAcctivitiName(), myProcess.getProcessInstanceId(), myProcess.getProcessDefinitionId(), User_Variable, task);
                            //放流程节点此时状态为审批通过
                            if (!SendMessage(name, loginName, myProcess.getAcctivitiName(), myProcess.getProcessInstanceId(), myProcess.getProcessDefinitionId(), User_Variable)) {




                           /* Map<String, Object> objectMap = task.getProcessVariables();
                            ActivitiParam activitiParam = JSONObject.parseObject(objectMap.get("data").toString(), ActivitiParam.class);
                            Object o = activitiParam.getData();
                            Integer s = processDao.selectOper(task.getProcessInstanceId());
                            //模块id
                            Integer ms = processDao.selectModel(s,task.getProcessInstanceId());
                            //-1属于知识库审批
                            if (ms==-1){
                                if (s == 0) {
                                    kenwSever.creatModelKenwSever(o, 1);
                                } else if (s == 1) {
                                    kenwSever.updateModelKenwSever(o, 1);
                                } else if (s == 2) {
                                    kenwSever.deleteModelKenwSever(o, 1);
                                }
                            }else {
                                if (s == 0) {
                                    modelSever.creatModelInstance(o, 1);
                                } else if (s == 2) {
                                    modelSever.updateModelInstance(o, 1);
                                } else if (s == 1) {
                                    modelSever.deleteModelInstance(o, 1);
                                }
                            }*/
                            }
                        }


                        map.put("type", OperMoudleReturn.BAND_PROCESS_SUCCESS.getType());
                        map.put("message", OperMoudleReturn.BAND_PROCESS_SUCCESS.getReason());
                    } else {
                        processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), "accept", false);
                        taskService.setVariable(task.getId(), "type", 0);
                        taskService.setVariable(task.getId(), task.getId(), 1);
                        taskService.setVariable(task.getId(), "accept", false);
                        /*     taskService.addComment(task.getId(), task.getProcessInstanceId(), myProcess.getComment());*/
                        taskService.claim(task.getId(), loginName);
                        /* taskService.complete(task.getId());*/
                        try {
                            runtimeService.deleteProcessInstance(b, "用户非流程绑定人/该流程用户已拒绝");
                        } catch (Exception e) {

                        }
                        List<MWUser> mwUsers = processDao.selectByDefintId(task.getTaskDefinitionKey());
                        if (mwUsers.size() > 0) {
                            mwMessageService.sendFailAlertMessage(name + "已提交，请知悉", mwUsers, name, false, null);
                        }
                        UpdateTaskList(myProcess.getProcessInstanceId(), 1);
                        //放流程节点此时状态为审批通过
                        map.put("type", OperMoudleReturn.BAND_PROCESS_UNUSER.getType());
                        map.put("message", OperMoudleReturn.BAND_PROCESS_UNUSER.getReason());
                    }
                    List<String> strings = userPremsion.get(task.getName());
                    //将所有工单提交为审批失败
                    //完成oa节点代办
                    SendOaMessage(name, loginName, myProcess.getAcctivitiName(), myProcess.getProcessInstanceId(), myProcess.getProcessDefinitionId(), User_Variable, task);

                    List<Integer> ids = new ArrayList<>();
                    String type = "";
                    for (String s : strings) {
                        if (s.contains("ROLE-")) {
                            type = "ROLE";
                            ids.add(Integer.valueOf(s.replace("ROLE-", "")));
                        }
                        if (s.contains("GROUP-")) {
                            type = "GROUP";
                            ids.add(Integer.valueOf(s.replace("GROUP-", "")));
                        }
                        if (s.contains("USER-")) {
                            type = "USER";
                            ids.add(Integer.valueOf(s.replace("USER-", "")));
                        }
                    }

                    List<MWUser> users = mwUserCommonService.selectByStringGroup(type, ids);
                    List<String> userLoginName = new ArrayList<>();
                    for (MWUser mwUser : users) {
                        userLoginName.add(mwUser.getLoginName());
                    }
                    insertOrUpdateCount(userLoginName, -1, 0);
                }
            }
        }


        return Reply.ok(map);
    }

    private void SendOaMessage(String taskName, String loginName, String processName, String processInstanceId, String defId, Integer UserId, Task task) {
        Map<String, List<String>> userPremsion = checkUser(defId);
        List<String> strings = userPremsion.get(task.getName());
        List<Integer> ids = new ArrayList<>();
        String type = "";
        for (String s : strings) {
            if (s.contains("ROLE-")) {
                type = "ROLE";
                ids.add(Integer.valueOf(s.replace("ROLE-", "")));
            }
            if (s.contains("GROUP-")) {
                type = "GROUP";
                ids.add(Integer.valueOf(s.replace("GROUP-", "")));
            }
            if (s.contains("USER-")) {
                type = "USER";
                ids.add(Integer.valueOf(s.replace("USER-", "")));
            }
            if (s.contains("USER_VARIABLE")) {
                ids.clear();
                ids.add(UserId);
            }
        }
        List<MWUser> users = mwUserCommonService.selectByStringGroup(type, ids);
        List<String> userLoginName = new ArrayList<>();
        for (MWUser mwUser : users) {
            userLoginName.add(mwUser.getLoginName());
        }
        insertOrUpdateCount(userLoginName, 1, 0);
        /*          String text = processName + ":" + loginName + "完成" + taskName + "审批," + "请您完成" + task.getName();*/

        try {
            sendOAModelOk(task.getId(), users, "请您完成" + taskName + "审批", 1);
        } catch (Exception e) {

        }
    }

    private void sendOAModelOk(String id, List<MWUser> mwUsers, String s, int i) throws IllegalAccessException {
        NotifyTodoRemoveContext notifyTodoSendContext = new NotifyTodoRemoveContext();
        notifyTodoSendContext.setModelName("猫维消息通知").setOptType(i)
                .setModelId(id);
        for (MWUser mwUser : mwUsers) {
            String qianUrl = maoweiUrl + "?username=" + mwUser.getUserName() + "&password=" + UrlCont(mwUser.getPassword()) + "&autoLogin=true&redirect=%2Fworkflow%2Fapproval";
            logger.info(qianUrl);
            TestOA.getInstance().setTodoDone(OAUrl, OAAdmin, OAPassword, mwUser.getOa(), notifyTodoSendContext);
            /*TestOA.getInstance().sendTodo(),*/
        }
    }

    private String UrlCont(String password) {
        password.replaceAll("=", "%3D");
        password.replaceAll("/", "%2F");
        password = password.replaceAll("\\+", "%2B");
        return password;
    }


    @Override
    public Reply checkMyTask(MyProcess myProcess) {
        Map<String, Object> map = new HashMap<>();
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        Integer mwRole = mwRoleService.selectByUserId(userId).getRoleId();
        String processInstanceId = myProcess.getProcessInstanceId();
        Integer modelId = 0;
        MyTaskTable myTaskTable = new MyTaskTable();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).includeProcessVariables().list();
        for (Task task : tasks) {
            Map<String, Object> kap = task.getProcessVariables();
            String name = task.getName();
            if (kap.get("startName") != null) {
                name = kap.get("startName").toString();
            }
            Map<String, List<String>> userPremsion = checkUser(task.getProcessDefinitionId());
            if (checkUsePremsion(userPremsion, userId, groupIds, mwRole, task.getName())) {
                String s = "cut" + task.getTaskDefinitionKey().split("cut")[1] + "cut";
                List<Map<String, Object>> mapList = processDao.selectByTaskId(task.getProcessDefinitionId(), s);
                if (mapList.size() > 0) {
                    Map<String, Object> kep = task.getProcessVariables();
                    modelId = Integer.parseInt(mapList.get(0).get("model_id").toString());
                    myTaskTable.setModelId(modelId);
                    myTaskTable.setMoudleId(Integer.parseInt(kep.get("modelId").toString()));
                    myTaskTable.setProcessInstanceId(processInstanceId);
                    myTaskTable.setProcessDefinitionId(task.getProcessDefinitionId());
                    myTaskTable.setProcessName(kep.get("processName").toString());
                    myTaskTable.setTaskName(task.getName());
                }
            }

        }
        return Reply.ok(myTaskTable);
    }

    private void UpdateTaskList(String processInstanceId, int i) {
        processDao.UpdateTaskList(processInstanceId, i);
    }

    @Override
    public Reply createProcessTwo(ProcessParam processParam) {
        Integer countBandOper = 1;
        //countBandOper = countBandOp(countBandOper, processParam.getProcessDefinition());
        ProcessNode processNode = processParam.getProcessDefinition().getChildNode().getChildNode();
        List<Integer> modeId = new ArrayList<>();


        BpmnModel bpmnModel = null;
        if (StringUtils.isNotEmpty(processParam.getProcessId())) {
            //查询流程信息
            bpmnModel = BpmnModelUtils.createnewProcess(processParam);
        } else {
            bpmnModel = BpmnModelUtils.createnewProcess(processParam);
        }
        List<Integer> set = processNode.getNodeInfo().getOperType();
        DeploymentBuilder builder = repositoryService.createDeployment();
        String processName = processParam.getProcessName() + ".bpmn";
        Deployment deploy = builder.name(processName)//申明流程名称
                .addBpmnModel(processName, bpmnModel)
                .deploy();//完成部署

        String activitiId = bpmnModel.getMainProcess().getId();

        //增加通知人群
        Map<String, List<Integer>> notifier = BpmnModelUtils.getNotifier();
        if (notifier.keySet().size() > 0) {
            processDao.insertTaskCompleteNotifier(notifier);
        }

        BpmnModelUtils.celanNotifier();

        String defId = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult().getId();

        List<ProcessNodeHaveful> havefuls = BpmnModelUtils.getNodeChild(processParam.getProcessDefinition().getChildNode(), processParam.getCustomIdPath());
        List<BindTask> bindTasks = new ArrayList<>();
        havefuls.stream().forEach(e -> {
            if (e.getType() == 2) {
                List<Integer> bill = new ArrayList<>();
                for (List<Integer> integers : e.getModelId()) {
                    bill.add(integers.get(integers.size() - 1));
                }
                for (Integer m : bill) {
                    BindTask bindTask = new BindTask();
                    bindTask.setTaskId(e.getNodeId());
                    bindTask.setModelId(m.toString());
                    if (bindTask.getTaskId().contains("cut0cut")) {
                        bindTask.setIsStart(1);
                    }
                    bindTasks.add(bindTask);
                }
            }
        });
        //模块绑定节点
        processDao.insetBandTask(activitiId, defId, bindTasks);

        ProcessDefDTO processDTO = new ProcessDefDTO();
        String processData = JSONObject.toJSONString(processParam);
        processDTO.setProcessData(processData);
        processDTO.setProcessInstanceKey(activitiId);
        processDTO.setVersion(deploy.getVersion());


        if (StringUtils.isEmpty(processParam.getProcessId())) {
            //新增流程定义
            processDTO.setProcessDefinitionId(defId);
            processDao.insertProcessDef(processDTO);
            processParam.setProcessId(defId);
        } else {
            //更新流程定义
            processDTO.setProcessDefinitionId(processParam.getProcessId());
            processDTO.setNewProcessDefinitionId(defId);
         /*   long count = runtimeService.createProcessInstanceQuery()
                    .processDefinitionId(processDTO.getProcessDefinitionId()).count();
            if (count == 0) {
                ActivitiUtils.deleteDeploymentByProcessId(processDTO.getProcessDefinitionId());
            }*/
            processDao.deleteProcessDef(2, processParam.getProcessId());

            processDao.updateProcessedToTable(processDTO.getNewProcessDefinitionId(), processParam.getProcessId());
            processDao.updateProcessedToMoudle(processDTO.getNewProcessDefinitionId(), processDTO.getProcessInstanceKey(), processParam.getProcessId());
            processDTO.setProcessDefinitionId(defId);
            processDao.insertProcessDef(processDTO);
            /*updateProcess(processDTO);*/
           /* DeleteDto deleteDto = DeleteDto.builder()
                    .type(DataType.PROCESS.getName())
                    .typeId(processParam.getProcessId()).build();
            mwCommonService.deleteMapperAndPerm(deleteDto);*/
        }
        //新增数据权限信息
        processParam.setProcessId(defId);
        updateMapperAndPerms(processParam);
        return Reply.ok(processDTO);
    }

    @Override
    public Reply disMoudle(ProcessParam processParam) {
        /*  processDao.insetBandTask(activitiId,defId,bindTasks);
         */
        Integer i = processDao.selectNumBind(processParam.getMoudleId());
        if (i > 0) {
            return Reply.fail("请去对应新增流程模块内解除后尝试");
        }
        processDao.insertMoudelBind(processParam.getProcessId(), processParam.getMoudleId());
        return Reply.ok("绑定对应模块成功");
    }

    @Override
    public Reply createMoudle(ProcessParam processParam) {
        Map<String, Object> map = processDao.selectmodelAndprocess(processParam.getMoudleId());
        BindTask bindTask = null;
        try {
            if (map != null) {
                bindTask = processDao.selectStartTask(map.get("activiti_process_id").toString());
            }
            bindTask.setPosition(processDao.selectPosition(processParam.getMoudleId()));
        } catch (Exception e) {

        }
        return Reply.ok(bindTask);
    }

    @Override
    public Reply candleMoudle(ProcessParam processParam) {
        processDao.candleMoudle(processParam.getMoudleId());
        return Reply.ok();
    }

    @Override
    public Reply addMoudleLine(ProcessParam processParam) {
        processDao.addMoudleLine(processParam.getMoudleId(), processParam.getPosition());
        return Reply.ok();
    }

    @Override
    public Reply proccessListBrowse(SearchProcessParam searchParam) {
        PageHelper.startPage(searchParam.getPageNumber(), searchParam.getPageSize());
        List<Map<String, Object>> mapList = processDao.proccessListBrowse(searchParam);
        List<Integer> instanceIds = new ArrayList<>();
        List<MwInstanceCommonParam> mapNames = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            instanceIds.add(Integer.parseInt(map.get("model_instance_id").toString()));
        }
        if (instanceIds.size() > 0) {
            mapNames = mwModelViewCommonService.getNameListByIds(instanceIds);
        }

        for (Map<String, Object> map : mapList) {
            for (MwInstanceCommonParam mwInstanceCommonParam : mapNames) {
                if (map.get("model_instance_id").toString().equals(mwInstanceCommonParam.getInstanceId().toString())) {
                    map.put("task_name", mwInstanceCommonParam.getInstanceName());
                    List<Integer> integers = new ArrayList<>();
                    List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().processInstanceId(map.get("process_instance_id").toString()).list();

                    Map<String, Object> objectMap = taskInstances.get(0).getProcessVariables();
                    for (String s : objectMap.keySet()) {
                        if (s.endsWith("cut")) {
                            integers.add(Integer.parseInt(objectMap.get(s).toString()));
                        }
                    }
                    map.put("model_instance_id", integers);
                }
            }

        }

        PageInfo pageInfo = new PageInfo<>(mapList);
        pageInfo.setList(mapList);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply getProcessInfo(TaskEntiy taskEntiy) {
        List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery().includeProcessVariables().processInstanceId(taskEntiy.getProcessInstanceId()).finished().list();
        NodeList nodeList = new NodeList();
        List<Node> nodes = new ArrayList<>();
        List<Integer> customIds = new ArrayList<>();
        customIds.add(-1);
        for (HistoricTaskInstance taskInstance : taskInstances) {
            String s[] = taskInstance.getTaskDefinitionKey().split("cut");
            Node node = new Node();
            Map<String, Object> kap = taskInstance.getProcessVariables();
            try {
                if (!(Boolean) kap.get(taskInstance.getTaskDefinitionKey() + "isComment")) {
                    node.setModelId(kap.get(taskInstance.getTaskDefinitionKey() + "moudleId").toString());
                    node.setInstanceId(Integer.parseInt(kap.get(taskInstance.getTaskDefinitionKey()).toString()));
                    node.setIsComment(true);
                } else {
                    node.setIsComment(false);
                    if (kap.get(taskInstance.getId() + "comment") != null) {
                        node.setComment(kap.get(taskInstance.getId() + "comment").toString());
                    } else {
                        node.setComment("同意");
                    }
                }


            } catch (Exception e) {
                node.setIsComment(false);
                if (kap.get(taskInstance.getId() + "comment") != null) {
                    node.setComment(kap.get(taskInstance.getId() + "comment").toString());
                } else {
                    node.setComment("同意");
                }
            }

            node.setAccept((Boolean) kap.get("accept"));
            node.setMoudleId((Integer) kap.get("modelId"));
            Integer custom = Integer.parseInt(s[1]);
            customIds.add(custom);
            node.setCustomerId(custom);
            nodes.add(node);
        }
        nodeList.setNodes(nodes);
        nodeList.setCustomerIds(customIds);
        return Reply.ok(nodeList);
    }

    @Override
    public Reply getMyTaskTable(MyTaskTable myTaskTable) {
        Integer User_Variable = 0;
        Map<String, Object> map = new HashMap<>();
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        Integer mwRole = mwRoleService.selectByUserId(userId).getRoleId();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(myTaskTable.getProcessInstanceId()).includeProcessVariables().list();
        for (Task task : tasks) {
            Map<String, Object> kap = task.getProcessVariables();
            String name = task.getName();
            if (kap.get("startName") != null) {
                name = kap.get("startName").toString();
            }
            if (kap.get("USER_VARIABLE") != null) {
                User_Variable = (Integer) kap.get("USER_VARIABLE");
            }
            if (task.getName().equals(myTaskTable.getTaskName())) {
                Map<String, List<String>> userPremsion = checkUser(task.getProcessDefinitionId());
                if (checkUsePremsion(userPremsion, userId, groupIds, mwRole, task.getName()) && myTaskTable.getType() == 1) {
                    processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), "accept", true);
                    taskService.setVariable(task.getId(), "type", 0);
                    taskService.setVariable(task.getId(), task.getId(), 0);
                    processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), task.getTaskDefinitionKey() + "isComment", false);
                    Reply reply = modelSever.creatModelInstance(myTaskTable.getNodeTable(), 1);
                    Integer shiliId = (Integer) reply.getData();
                    processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), task.getTaskDefinitionKey(), shiliId);
                    processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), task.getTaskDefinitionKey() + "moudleId", myTaskTable.getModelId());
                    processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), task.getTaskDefinitionKey() + "isComment", false);

                    taskService.setVariable(task.getId(), task.getId(), 0);
                    taskService.claim(task.getId(), loginName);
                    taskService.complete(task.getId());
                    List<MWUser> mwUsers = processDao.selectByDefintId(task.getTaskDefinitionKey());
                    if (mwUsers.size() > 0) {
                        mwMessageService.sendFailAlertMessage(name + "已提交，请知悉", mwUsers, name, false, null);
                    }


                    if (myTaskTable.getModelId() != 2) {
                        //完成oa节点代办
                        SendOaMessage(name, loginName, myTaskTable.getAcctivitiName(), myTaskTable.getProcessInstanceId(), myTaskTable.getProcessDefinitionId(), User_Variable, task);
                        //生成列表数据
                        //放流程节点此时状态为审批通过
                        SendMessage(name, loginName, myTaskTable.getProcessName(), myTaskTable.getProcessInstanceId(), myTaskTable.getProcessDefinitionId(), User_Variable);
                        map.put("type", OperMoudleReturn.BAND_PROCESS_SUCCESS.getType());
                        map.put("message", OperMoudleReturn.BAND_PROCESS_SUCCESS.getReason());
                        List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(myTaskTable.getProcessInstanceId()).includeProcessVariables().list();
                        if (nextTasks.size() == 0) {
                            //判断 审批已经完成 将表单状态完成
                            UpdateTaskList(myTaskTable.getProcessInstanceId(), 0);
                        }
                        //放流程节点此时状态为审批通过
                        if (!SendMessage(name, loginName, myTaskTable.getAcctivitiName(), myTaskTable.getProcessInstanceId(), myTaskTable.getProcessDefinitionId(), User_Variable)) {

                           /* Map<String, Object> objectMap = task.getProcessVariables();
                            ActivitiParam activitiParam = JSONObject.parseObject(objectMap.get("data").toString(), ActivitiParam.class);
                            Object o = activitiParam.getData();
                            Integer s = processDao.selectOper(task.getProcessInstanceId());
                            //模块id
                            Integer ms = processDao.selectModel(s,task.getProcessInstanceId());
                            //-1属于知识库审批
                            if (ms==-1){
                                if (s == 0) {
                                    kenwSever.creatModelKenwSever(o, 1);
                                } else if (s == 1) {
                                    kenwSever.updateModelKenwSever(o, 1);
                                } else if (s == 2) {
                                    kenwSever.deleteModelKenwSever(o, 1);
                                }
                            }else {
                                if (s == 0) {
                                    modelSever.creatModelInstance(o, 1);
                                } else if (s == 2) {
                                    modelSever.updateModelInstance(o, 1);
                                } else if (s == 1) {
                                    modelSever.deleteModelInstance(o, 1);
                                }
                            }*/
                        }
                    }


                    map.put("type", OperMoudleReturn.BAND_PROCESS_SUCCESS.getType());
                    map.put("message", OperMoudleReturn.BAND_PROCESS_SUCCESS.getReason());
                } else {
                    processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), "accept", false);
                    taskService.setVariable(task.getId(), "type", 0);
                    taskService.setVariable(task.getId(), task.getId(), 1);
                    taskService.setVariable(task.getId(), "accept", false);
                    /*      taskService.addComment(task.getId(), task.getProcessInstanceId(), myTaskTable.getComment());*/
                    taskService.claim(task.getId(), loginName);

                    /* taskService.complete(task.getId());*/
                    try {
                        runtimeService.deleteProcessInstance(myTaskTable.getProcessInstanceId(), "用户非流程绑定人/该流程用户已拒绝");
                    } catch (Exception e) {

                    }
                    UpdateTaskList(myTaskTable.getProcessInstanceId(), 1);
                    //放流程节点此时状态为审批通过
                    map.put("type", OperMoudleReturn.BAND_PROCESS_UNUSER.getType());
                    map.put("message", OperMoudleReturn.BAND_PROCESS_UNUSER.getReason());
                    List<MWUser> mwUsers = processDao.selectByDefintId(task.getTaskDefinitionKey());
                    if (mwUsers.size() > 0) {
                        mwMessageService.sendFailAlertMessage(name + "已提交，请知悉", mwUsers, name, false, null);
                    }
                }
                List<String> strings = userPremsion.get(task.getName());
                List<Integer> ids = new ArrayList<>();

                String type = "";
                for (String s : strings) {
                    if (s.contains("ROLE-")) {
                        type = "ROLE";
                        ids.add(Integer.valueOf(s.replace("ROLE-", "")));
                    }
                    if (s.contains("GROUP-")) {
                        type = "GROUP";
                        ids.add(Integer.valueOf(s.replace("GROUP-", "")));
                    }
                    if (s.contains("USER-")) {
                        type = "USER";
                        ids.add(Integer.valueOf(s.replace("USER-", "")));
                    }
                }
                List<MWUser> users = mwUserCommonService.selectByStringGroup(type, ids);
                List<String> userLoginName = new ArrayList<>();


                for (MWUser mwUser : users) {
                    userLoginName.add(mwUser.getLoginName());
                }
                insertOrUpdateCount(userLoginName, -1, 0);
            }
        }
        return Reply.ok(map);
    }

    @Override
    public Reply browseDelete(SearchProcessParam searchParam) {
        processDao.proccessListDelete(searchParam.getIds());
        return Reply.ok();
    }


    private List<String> getTaskName(String id) {

        /*BpmnModel bpmnModel = repositoryService.getBpmnModel(defId);

        //System.out.println(bpmnModel);
        Map<String, List<String>> listMap = new HashMap<>();

        Map<String, FlowElement> map = bpmnModel.getProcesses().get(0).getFlowElementMap();
        for (FlowElement element : map.values()) {
            if (element.getClass().toString().contains("UserTask")) {
                UserTask userTask = ((UserTask) element);
                if (userTask.getCandidateUsers().size() > 0) {
                    listMap.put(element.getName(), userTask.getCandidateUsers());
                } else if (userTask.getCandidateGroups().size() > 0) {
                    listMap.put(element.getName(), userTask.getCandidateGroups());
                } else {
                    listMap.put(element.getName(), userTask.getCandidateUsers());
                }

            }
        }
        return listMap;*/
        return null;
    }


    public String startProcess(String processId, Map<String, Object> variables) {
        if (StringUtils.isEmpty(processId)) {
            log.info("processId is empty");
            return null;
        }
        try {
            ProcessInstance instance = runtimeService.startProcessInstanceById(processId, variables);
            runtimeService.setVariables(instance.getId(), variables);
            log.info("startProcess:" + instance.getId());
            Task task = taskService.createTaskQuery().processInstanceId(instance.getId()).active().singleResult();
            ;
            log.info(task.getName());
            return instance.getId();
        } catch (Exception e) {
            log.error("startProcess", e);
        }

        return null;
    }

    public Map<String, Object> getInstanceVariable(String instanceId) {
        return runtimeService.getVariables(instanceId);
    }

    //根据用户信息查询相关流程实例
    public List<ProcessInstanceDTO> searchHisProcessInstance(ProcessInstanceParam param) {
        //创建查询对象，查询该用户发起的流程
        LoginContext loginContext = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName());
        Integer userId = loginContext.getUserId();
        List<String> taskCandidateGroup = BpmnModelUtils.getTaskCandidateGroup();

        if (null == userId) {
            return null;
        }

        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .taskCandidateUser(userId.toString()).or().taskCandidateGroupIn(taskCandidateGroup).finished()
                .listPage(param.getPageNumber() - 1, param.getPageSize());

        List<ProcessInstanceDTO> processInstanceDTOS = new ArrayList<>();
        if (null != list) {
            for (HistoricTaskInstance historicTaskInstance : list) {
                ProcessInstanceDTO processInstanceDTO = new ProcessInstanceDTO();
                processInstanceDTO.setProcessInstanceName(historicTaskInstance.getName());
                processInstanceDTO.setAssignee(historicTaskInstance.getAssignee());
            }
        }
        return processInstanceDTOS;
    }

    public long countHisProcessInstance(ProcessInstanceParam param) {
        //创建查询对象，查询该用户发起的流程
        LoginContext loginContext = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName());
        Integer userId = loginContext.getUserId();
        List<String> taskCandidateGroup = BpmnModelUtils.getTaskCandidateGroup();

        if (null == userId) {
            return 0;
        }

        long count = historyService.createHistoricTaskInstanceQuery()
                .taskCandidateUser(userId.toString()).or().taskCandidateGroupIn(taskCandidateGroup).finished().count();

        return count;
    }

    private void updateMapperAndPerms(ProcessParam uParam) {
        List<String> list = new ArrayList<>();
        list.add(uParam.getProcessId());

        List<List<Integer>> orgs = new ArrayList<List<Integer>>();
        orgs.add(uParam.getOrg());

        if (null != uParam.getGroup() && uParam.getGroup().size() > 0) {
            uParam.setGroupIdscheckbox(true);
        }

        if (null != uParam.getOrg() && uParam.getOrg().size() > 0) {
            uParam.setOrgIdscheckbox(true);
        }

        if (null != uParam.getUser() && uParam.getUser().size() > 0) {
            uParam.setPrincipalcheckbox(true);
        }

        UpdateDTO updateDTO = UpdateDTO.builder()
                .isGroup(uParam.isGroupIdscheckbox())
                .isUser(uParam.isPrincipalcheckbox())
                .isOrg(uParam.isOrgIdscheckbox())
                .groupIds(uParam.getGroup())  //用户组
                .userIds(uParam.getUser()) //责任人
                .orgIds(orgs) //机构
                .typeIds(list)    //批量流程数据主键
                .type(DataType.PROCESS.getName())
                .desc(DataType.PROCESS.getDesc()).build(); //资产
        mwCommonService.editorMapperAndPerms(updateDTO);
    }

    @Override
    public Map<String, Object> OperMoudleContainActiviti(String moudleId, Integer operInt, Object data) {
        List<String> processInstanceKey = processDao.selectProcessInstanceKey(moudleId, operInt);
        Map<String, Object> map = new HashMap<>();
        if (processInstanceKey.size() == 0) {
            map.put("type", OperMoudleReturn.UNBAND.getType());
            map.put("message", OperMoudleReturn.UNBAND.getReason());
        } else if (processInstanceKey.size() != 1) {
            map.put("type", OperMoudleReturn.BAND_PROCESS_MORE.getType());
            map.put("message", OperMoudleReturn.BAND_PROCESS_MORE.getReason());
        } else {
            //开始启动流程
            String activitiId = processInstanceKey.get(0);
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(activitiId);
            String processInstanceId = processInstance.getId(); //启动流程ID
            String processDefinitionId = processInstance.getProcessDefinitionId(); //启动流程所有的配置id
            Integer status = 0; //默认启用失败
//            processDao.insertStartProcessAndMoudle(startId,moudleId);

            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            Integer mwRole = mwRoleService.selectByUserId(userId).getRoleId();
            Map<String, List<String>> userPremsion = checkUser(processDefinitionId);

            String name = "";
            if (operInt == 0) {
                name = "添加";
            }
            if (operInt == 1) {
                name = "删除";
            }
            if (operInt == 2) {
                name = "编辑";
            }
            String kill = "";
            if (Integer.parseInt(moudleId) == -1) {
                if (operInt == 0) {
                    name = "添加";
                }
                if (operInt == 2) {
                    name = "删除";
                }
                if (operInt == 1) {
                    name = "编辑";
                }
                name = name + "知识库知识";
            } else {
                kill = (String) modelSever.selectModelNameById(Integer.parseInt(moudleId)).getData();
                name = name + "[" + kill + "]资产审批";
            }


            Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
            if (checkUsePremsion(userPremsion, userId, groupIds, mwRole, task.getName())) {
                processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), "accept", true);
                status = 1;//流程成功部署
                ActivitiParam activitiParam = new ActivitiParam();
                activitiParam.setAccept(true);
                activitiParam.setData(data);
                taskService.setVariable(task.getId(), "data", activitiParam);
                taskService.setVariable(task.getId(), "type", 0);
                taskService.setVariable(task.getId(), "acctivitiName", processInstance.getProcessDefinitionName());
                taskService.setVariable(task.getId(), "submitBandModelName", name);
                taskService.setVariable(task.getId(), "submitBandUser", loginName);
                taskService.setVariable(task.getId(), "submitTime", new Date());
                taskService.setVariable(task.getId(), task.getId(), 0);
                taskService.claim(task.getId(), loginName);
                taskService.complete(task.getId());
                //放流程节点此时状态为审批通过
                SendMessage(task.getName(), loginName, processInstance.getProcessDefinitionName(), processInstanceId, processDefinitionId, 0);
                map.put("type", OperMoudleReturn.BAND_PROCESS_SUCCESS.getType());
                map.put("message", OperMoudleReturn.BAND_PROCESS_SUCCESS.getReason());
            } else {
                processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), "accept", false);
                ActivitiParam activitiParam = new ActivitiParam();
                activitiParam.setAccept(false);
                activitiParam.setData(data);

                //添加流程的绑定模型名称
                taskService.setVariable(task.getId(), "data", activitiParam);
                taskService.setVariable(task.getId(), "acctivitiName", processInstance.getProcessDefinitionName());
                taskService.setVariable(task.getId(), "submitBandModelName", name);
                taskService.setVariable(task.getId(), "submitBandUser", loginName);
                taskService.setVariable(task.getId(), "submitTime", new Date());
                taskService.setVariable(task.getId(), "type", 0);
                taskService.setVariable(task.getId(), task.getId(), 1);
                taskService.claim(task.getId(), loginName);
                taskService.complete(task.getId());
                //放流程节点此时状态为审批通过
                runtimeService.deleteProcessInstance(processInstanceId, "用户非流程绑定人");
                map.put("type", OperMoudleReturn.BAND_PROCESS_UNUSER.getType());
                map.put("message", OperMoudleReturn.BAND_PROCESS_UNUSER.getReason());
            }
            processDao.insertProcessMyTask(task.getProcessInstanceId(), loginName);
            processDao.insertProcessStartActiviti(moudleId, processInstanceId, processDefinitionId, status, operInt);
        }
        return map;
    }


    //启动流程或者完成表单实例
    @Override
    public Map<String, Object> OperMoudleContainActivitiTwo(String moudleId, Integer operInt, Object data, Integer modelId, Map<String, Object> mapKey /*,String processInstance*/) {
        //查询部署id和定义id
        List<Map<String, Object>> processTask = processDao.selectProcessTaskBindModel(moudleId, modelId);
        //返回通信机制
        Map<String, Object> map = new HashMap<>();
        Integer userVariable = 0;
        if (checkMoudelAndModel(moudleId,modelId)){
            if (processTask != null) {
                if (processTask.size() > 1) {
                    //已经绑定流程
                    map.put("type", OperMoudleReturn.BAND_PROCESS_MORE.getType());
                    map.put("message", OperMoudleReturn.BAND_PROCESS_MORE.getReason());
                } else if (processTask.size() == 0) {
                    //没有绑定流程
                    map.put("type", OperMoudleReturn.UNBAND.getType());
                    map.put("message", OperMoudleReturn.UNBAND.getReason());
                } else {
                    Map<String, Object> task = processTask.get(0);

                    //流程为起始节点
                    String activitiId = (String) task.get("start_id");
                    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(activitiId);

                    String processInstanceId = processInstance.getId(); //启动流程ID
                    String processDefinitionId = processInstance.getProcessDefinitionId(); //启动流程所有的配置id
                    Integer status = 0; //默认启用失败
//            processDao.insertStartProcessAndMoudle(startId,moudleId);

                    String loginName = iLoginCacheInfo.getLoginName();
                    Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
                    List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
                    Integer mwRole = mwRoleService.selectByUserId(userId).getRoleId();
                    Map<String, List<String>> userPremsion = checkUser(processDefinitionId);
                    String name = "";
                    if (operInt == 0) {
                        name = "添加";
                    }
                    if (operInt == 1) {
                        name = "删除";
                    }
                    if (operInt == 2) {
                        name = "编辑";
                    }
                    String kill = "";
                    if (Integer.parseInt(moudleId) == -1) {
                        if (operInt == 0) {
                            name = "添加";
                        }
                        if (operInt == 2) {
                            name = "删除";
                        }
                        if (operInt == 1) {
                            name = "编辑";
                        }
                        name = name + "知识库知识";
                    } else {
                        kill = (String) modelSever.selectModelNameById(Integer.parseInt(moudleId)).getData();
                        name = name + "[" + kill + "]工单审批";
                    }
                    Task processBoundTask = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();

                    if (checkUsePremsion(userPremsion, userId, groupIds, mwRole, processBoundTask.getName())) {
                        if (checkVariable(userPremsion.get(processBoundTask.getName()))) {
                            processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), "USER_VARIABLE", userId);
                        }
                        //完成发起人节点
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), "accept", true);
                        //绑定模块的id
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), "modelId", modelId);


                        status = 1;//流程成功部署
                        ActivitiParam activitiParam = new ActivitiParam();
                        activitiParam.setAccept(true);
                        activitiParam.setData(data);
                        for (String s : mapKey.keySet()) {
                            if (s.contains("USER_")) {
                                userVariable = (Integer) mapKey.get(s);
                            }
                            taskService.setVariable(processBoundTask.getId(), s, mapKey.get(s));
                        }


                        //用来生成实力表单并且返回
                        Reply reply = modelSever.creatModelInstance(data, 1);
                        Integer shiliId = (Integer) reply.getData();
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), processBoundTask.getTaskDefinitionKey(), shiliId);
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), processBoundTask.getTaskDefinitionKey() + "moudleId", moudleId);
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), processBoundTask.getTaskDefinitionKey() + "isComment", false);
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), "model_instance_id", shiliId);

                        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processBoundTask.getProcessDefinitionId()).singleResult();
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), "processName", processDefinition.getName());

                        //获取表单名称
                        try {
                            List<Integer> integers = new ArrayList<>();
                            integers.add(shiliId);
                            List<MwInstanceCommonParam> mwInstanceCommonParams = mwModelViewCommonService.getNameListByIds(integers);
                            kill = mwInstanceCommonParams.get(0).getInstanceName();
                        } catch (Exception e) {

                        }
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), "startName", kill);


                        //事件回传跟进人
                        if (modelId==2){
                            List<MwModelPropertyInfo> propertyInfos = mwModelCommonService.getModelPropertyInfoByModelId(Integer.parseInt(moudleId));
                            String follower = checkSubmitor(mapKey,"跟进人",propertyInfos);
                            String followerType = checkSubmitor(mapKey,"事件类型",propertyInfos);
                            createTaskList(kill, follower, 2, modelId, shiliId, moudleId, processBoundTask.getProcessInstanceId(),followerType);
                        }else {
                            //生成列表数据
                            createTaskList(kill, loginName, 2, modelId, shiliId, moudleId, processBoundTask.getProcessInstanceId(),null);
                        }

                        taskService.claim(processBoundTask.getId(), loginName);
                        taskService.complete(processBoundTask.getId());
                        List<MWUser> mwUsers = processDao.selectByDefintId(processBoundTask.getTaskDefinitionKey());

                        if (modelId != 2) {
                            if (mwUsers.size() > 0) {
                                mwMessageService.sendFailAlertMessage(kill + "已提交，请知悉", mwUsers, name, false, null);
                                try {
                                    sendOAModel(processBoundTask.getId(), mwUsers, kill + "已提交，请知悉", 2);
                                } catch (Exception e) {

                                }

                            }
                            //放流程节点此时状态为审批通过
                            SendMessage(kill, loginName, processInstance.getProcessDefinitionName(), processInstanceId, processDefinitionId, userVariable);
                        }


                        map.put("type", OperMoudleReturn.BAND_PROCESS_SUCCESS.getType());
                        map.put("message", OperMoudleReturn.BAND_PROCESS_SUCCESS.getReason());

                    } else {
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), "accept", false);
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), processBoundTask.getProcessDefinitionId() + "moudleId", moudleId);
                        ActivitiParam activitiParam = new ActivitiParam();
                        activitiParam.setAccept(false);
                        activitiParam.setData(data);

                        //添加流程的绑定模型名称
                        for (String s : mapKey.keySet()) {
                            taskService.setVariable(processBoundTask.getId(), s, mapKey.get(s));
                        }
                        taskService.claim(processBoundTask.getId(), loginName);
                        taskService.complete(processBoundTask.getId());
                        Reply reply = modelSever.creatModelInstance(data, 1);
                        Integer shiliId = (Integer) reply.getData();
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), processBoundTask.getProcessDefinitionId(), shiliId);
                        //放流程节点此时状态为审批通过
                        /*createTaskList(processBoundTask.getName(),loginName,1,modelId,shiliId,moudleId,processBoundTask.getProcessInstanceId());*/
                        processEngine.getRuntimeService().setVariable(processBoundTask.getProcessInstanceId(), "model_instance_id", shiliId);
                        runtimeService.deleteProcessInstance(processInstanceId, "用户非流程绑定人");
                        map.put("type", OperMoudleReturn.BAND_PROCESS_UNUSER.getType());
                        map.put("message", OperMoudleReturn.BAND_PROCESS_UNUSER.getReason());
                    }
                    processDao.insertProcessMyTask(processBoundTask.getProcessInstanceId(), loginName);
                    processDao.insertProcessStartActiviti(moudleId, processInstanceId, processDefinitionId, status, operInt);
                }
            }
        }
        else {
            map.put("type", OperMoudleReturn.submitMore.getType());
            map.put("message", OperMoudleReturn.submitMore.getReason());
        }
        return map;
    }

    private boolean checkMoudelAndModel(String moudleId, Integer modelId) {
       Integer count =  processDao.checkMoudelAndModel(moudleId, modelId);
        if (count>0){
            return true;
        }
        return false;
    }

    private String checkSubmitor( Map<String, Object> mapKey, String keyname,List<MwModelPropertyInfo> mwModelPropertyInfos) {

        String id = "";
        String  loginName = "表单内未填写";
        try {
            //找到跟进人
            for ( MwModelPropertyInfo mwModelPropertyInfo:mwModelPropertyInfos) {
                if (mwModelPropertyInfo.getPropertyName().equals(keyname)){
                    id = mwModelPropertyInfo.getPropertyIndexId();
                }
            }

            for (String key:mapKey.keySet()) {
                if (key.equals(id)){
                    loginName= mapKey.get(key).toString();
                }
            }
        }catch (Exception e)
        {
            loginName = "表单内未填写";
            logger.error(e.toString());
        }

        return loginName;
    }

    //发送oa系统
    private void sendOAModel(String id, List<MWUser> mwUsers, String s, int i) throws Exception {
        NotifyTodoSendContext notifyTodoSendContext = new NotifyTodoSendContext();
        notifyTodoSendContext.setModelName("猫维消息通知").setCreateTime(SeverityUtils.getDate(new Date().getTime() / 1000)).setType(i)
                .setSubject(s).setModelId(id);

        for (MWUser mwUser : mwUsers) {
            String qianUrl = maoweiUrl + "?username=" + mwUser.getUserName() + "&password=" + UrlCont(mwUser.getPassword()) + "&autoLogin=true&redirect=%2Fworkflow%2Fapproval";
            logger.info(qianUrl);
            notifyTodoSendContext.setLink(qianUrl).setMobileLink(qianUrl).setPadLink(qianUrl);
            TestOA.getInstance().sendTodo(OAUrl, OAAdmin, OAPassword, mwUser.getOa(), notifyTodoSendContext);
            /*TestOA.getInstance().sendTodo(),*/
        }


    }

    private void createTaskList(String name, String loginName, Integer successful, Integer modelId, Integer instanceId, String mouldId, String processInstanceId,String type) {
        processDao.createTaskList(name, loginName, successful, modelId, instanceId, mouldId, processInstanceId,type);

    }

    private boolean SendMessage(String taskName, String loginName, String processName, String processInstanceId, String defId, Integer UserId) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        Boolean testful = true;
        for (Task task : tasks) {
            if (task != null) {
                Map<String, List<String>> userPremsion = checkUser(defId);
                List<String> strings = userPremsion.get(task.getName());
                List<Integer> ids = new ArrayList<>();
                String type = "";
                for (String s : strings) {
                    if (s.contains("ROLE-")) {
                        type = "ROLE";
                        ids.add(Integer.valueOf(s.replace("ROLE-", "")));
                    }
                    if (s.contains("GROUP-")) {
                        type = "GROUP";
                        ids.add(Integer.valueOf(s.replace("GROUP-", "")));
                    }
                    if (s.contains("USER-")) {
                        type = "USER";
                        ids.add(Integer.valueOf(s.replace("USER-", "")));
                    }
                    if (s.contains("USER_VARIABLE")) {
                        ids.clear();
                        ids.add(UserId);
                    }
                }
                List<MWUser> users = mwUserCommonService.selectByStringGroup(type, ids);
                List<String> userLoginName = new ArrayList<>();
                for (MWUser mwUser : users) {
                    userLoginName.add(mwUser.getLoginName());
                }
                insertOrUpdateCount(userLoginName, 1, 0);
                /*          String text = processName + ":" + loginName + "完成" + taskName + "审批," + "请您完成" + task.getName();*/
                String text = "请您完成" + taskName + "审批";
                mwMessageService.sendFailAlertMessage(text, users, processName, false, null);
                try {
                    sendOAModel(task.getId(), users, "请您完成" + taskName + "审批", 1);
                } catch (Exception e) {

                }


            } else {
                testful = false;
            }
        }
        return testful;
    }

    //type表示是0.更新还是1.加减
    private void insertOrUpdateCount(List<String> userLoginName, int i, int type) {
        try{
            List<String> strings = processDao.selectHaveProcess(userLoginName);
            List<String> addstrings = new ArrayList<>();
            for (String s : userLoginName) {
                if (strings.contains(s)) {

                } else {
                    addstrings.add(s);
                }
            }
            //type
            if (strings.size() > 0) {
                processDao.UpdateCount(strings, i, type);
            }
            if (addstrings.size() > 0) {
                processDao.insertProcessCount(addstrings, i);
            }
        }catch (Exception e){

        }
        //common

    }


    /*
     * 验证用户是否是工作流
     * */
    Map<String, List<String>> checkUser(String defId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(defId);
        Map<String, List<String>> listMap = new HashMap<>();

        Map<String, FlowElement> map = bpmnModel.getProcesses().get(0).getFlowElementMap();
        for (FlowElement element : map.values()) {
            if (element.getClass().toString().contains("UserTask")) {
                UserTask userTask = ((UserTask) element);
                if (userTask.getCandidateUsers().size() > 0) {
                    listMap.put(element.getName(), userTask.getCandidateUsers());
                } else if (userTask.getCandidateGroups().size() > 0) {
                    listMap.put(element.getName(), userTask.getCandidateGroups());
                } else {
                    listMap.put(element.getName(), userTask.getCandidateUsers());
                }

            }
        }
        return listMap;
    }

    /*
     * 验证用户之前是否有审批
     * */
    public boolean checkUserSignAgin(String username, String instanceId) {
        List<HistoricActivityInstance> list = processEngine.getHistoryService() // 历史相关Service
                .createHistoricActivityInstanceQuery() // 创建历史活动实例查询
                .processInstanceId(instanceId)
                .taskAssignee(username)// 执行流程实例id
                .finished()
                .list();
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean checkUsePremsion(Map<String, List<String>> listMap, Integer users, List<Integer> groups, Integer mwRole, String name) {
        if (name != null) {
            List<String> s = listMap.get(name);
            if (check(s, users, groups, mwRole)) {
                return true;
            }

        } else {
            for (List<String> s : listMap.values()) {
                if (check(s, users, groups, mwRole)) {
                    return true;
                }
            }
        }
        return false;
    }

    //查询是不是流程用户变量
    public boolean checkVariable(List s) {
        if (s.contains("USER_VARIABLE")) {
            return true;
        }
        return false;
    }


    public boolean check(List s, Integer users, List<Integer> groups, Integer mwRole) {
        if (s.contains("USER-" + users.toString())) {
            return true;
        }
        for (Integer use : groups) {
            if (s.contains("GROUP-" + use)) {
                return true;
            }
        }
        if (s.contains("ROLE-" + mwRole.toString())) {
            return true;
        }
        return false;
    }


}
