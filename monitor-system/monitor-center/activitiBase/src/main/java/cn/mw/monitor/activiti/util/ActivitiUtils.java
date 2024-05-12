package cn.mw.monitor.activiti.util;

import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.activiti.dto.FlowNodeClazzEnum;
import cn.mw.monitor.activiti.dto.FlowNodeDTO;
import cn.mw.monitor.activiti.dto.FlowViewDataDTO;
import cn.mw.monitor.activiti.dto.SequenceFlowDTO;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2020/9/24 9:23
 * @Version 1.0
 */
@Slf4j
public class ActivitiUtils {
    /**
     * 创建流程引擎对象
     */
    private static ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    /**
     * 运行流程使用的runtimeService
     */
    private static RuntimeService runtimeService = processEngine.getRuntimeService();
    /**
     * 查看用户任务的完成度用taskService
     */
    private static TaskService taskService = processEngine.getTaskService();
    /**
     * 查看用户历史记录用historyService
     */
    private static HistoryService historyService = processEngine.getHistoryService();
    /**
     * 获取仓库服务
     */
    private static RepositoryService repositoryService = processEngine.getRepositoryService();

    /**
     * 部署流程定义
     *
     * @param name     流程名称
     * @param bpmnPath 流程文件路径
     * @return
     * @throws Exception
     */
    public static String deploymentProcessDefinition(String name, String bpmnPath) throws Exception {
        //创建发布配置对象
        DeploymentBuilder builder = repositoryService.createDeployment();
        Deployment deploy = builder.name(name)//申明流程名称
                .addClasspathResource(bpmnPath)//加载资源文件，一次只能加载一个文件
                .deploy();//完成部署
        return deploy.getId();
    }

    /**
     * 删除流程部署定义
     *
     * @param deploymentId
     * @throws Exception
     */
    public static void deleteDeployment(String deploymentId) throws Exception {
        //普通删除，如果当前规则下有正在执行的流程，会报异常
//        repositoryService.deleteDeployment(deploymentId);
//        //级联删除，会删除和当前规则相关的所有信息，包括历史
        repositoryService.deleteDeployment(deploymentId, true);
    }

    public static void deleteDeploymentByProcessId(String processId) throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processId).singleResult();
        repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
    }

    public static InputStream viewImage(String processId, HttpServletResponse response) throws Exception {
        InputStream imageStream = null;
        ServletOutputStream outputStream = null;
        try {
            if (StringUtils.isBlank(processId)) {
                log.error("流程实例为空");
            }
            //获取历史流程实例
            HistoricProcessInstance instance = findProcessInstanceByProcessInstanceId(processId);
            //获取流程定义id
            String processDefinitionId = instance.getProcessDefinitionId();
            //获取流程定义信息
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

            DefaultProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
            ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
            //获取流程历史中已执行节点
            List<HistoricActivityInstance> historicActivityInstances = getHistoricActivityInstance(processId);
            //高亮环节id集合
            List<String> highLightedActivitis = new ArrayList<>();
            for (HistoricActivityInstance tempActivity : historicActivityInstances) {
                highLightedActivitis.add(tempActivity.getActivityId());
            }
            //高亮线路id集合
            List<String> highLightedFlows = getHighLightedFlows(bpmnModel, historicActivityInstances);
            Set<String> currIds = runtimeService.createExecutionQuery().processInstanceId(processId).list().stream().map(e -> e.getActivityId()).collect(Collectors.toSet());

            imageStream = processDiagramGenerator.generateDiagram(bpmnModel, highLightedActivitis, highLightedFlows, "宋体", "宋体", null);
            return imageStream;
            //输出流程图
//            outputStream = response.getOutputStream();
//            byte[] b = new byte[2048];
//            int len;
//            while ((len = imageStream.read(b, 0, b.length)) != -1) {
//                outputStream.write(b, 0, len);
//            }
        } catch (Exception e) {
            throw new RuntimeException("获取流程图出错", e);
        }
    }

    /**
     * 运行流程（根据key来启动  会启动最新版本号的流程定义）
     *
     * @param instanceKey 流程图的id
     * @param variables   流程的变量
     * @return
     */
    public static String startProcess(String instanceKey, Map<String, Object> variables) {
        try {
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(instanceKey, variables);
            return instance.getId();
        } catch (Exception e) {
            log.error("fail to startProcess instanceKey:{} variables:{} cause:{}", instanceKey, variables, e.getMessage());
        }
        return null;
    }

    /**
     * 提交给领导审核
     *
     * @param approve 领导信息
     * @param taskId  任务id
     * @return
     */
    public static void completeKnowledgeTask(String approve, String taskId) {
        try {
            HashMap<String, Object> variables = new HashMap<>();
            variables.put("approve", approve);
            taskService.complete(taskId, variables);
        } catch (Exception e) {
            log.error("fail to completeKnowledgeTask approve:{} taskId{} cause:{}", approve, taskId, e.getMessage());
        }
    }

    /**
     * 查询当前创建任务流程的任务Id
     *
     * @param assignee  任务的办理人
     * @param processId 流程实例id
     * @return
     */
    public static String queryKnowledgeProcessTaskId(String assignee, String processId) {
        try {
            Task task = taskService.createTaskQuery()
                    .processInstanceId(processId)
                    .taskAssignee(assignee)
                    .singleResult();
            if (task != null) {
                return task.getId();
            }
        } catch (Exception e) {
            log.error("fail to queryKnowledgeProcessTaskId assignee:{}  processId:{} cause:{}", assignee, processId, e.getMessage());
        }
        return null;
    }

    /**
     * 删除流程实例
     *
     * @param processId 流程实例id
     */
    public static void deleteProcess(String processId) {
        try {
            //判断该流程实例是否结束，未结束和结束删除的表的信息是不一样的
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processId)
                    .singleResult();
            if (pi == null) {
                //该流程实例已经完成了
                historyService.deleteHistoricProcessInstance(processId);
            } else {
                //该流程实例未结束的
                runtimeService.deleteProcessInstance(processId, "");
                historyService.deleteHistoricProcessInstance(processId);
            }
        } catch (Exception e) {
            log.error("fail to deleteProcess  processId:{} cause:{}", processId, e.getMessage());
        }
    }

    /**
     * 根据流程实例ID和任务key值查询所有同级任务集合
     *
     * @param processInstanceId
     * @param key
     * @return
     */
    public List<Task> findTaskListByKey(String processInstanceId, String key) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(key).list();
    }

    /**
     * 根据用户,用户组,角色查询所有任务个数
     *
     * @param userId
     * @param groups
     * @param roles
     * @return long
     */
    public long countTaskListByUserGroupRole(String userId, List<String> groups, List<String> roles) {
        List<String> mergeGroups = new ArrayList<>();
        mergeGroups.addAll(groups);
        mergeGroups.addAll(roles);
        return taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .taskCandidateGroupIn(mergeGroups).count();
    }

    /**
     * 根据用户,用户组,角色查询所有任务
     *
     * @param userId
     * @param groups
     * @param roles
     * @param pageNum
     * @param pageSize
     * @return List<Task>
     */
    public List<Task> findTaskListByUserGroupRole(String userId, List<String> groups, List<String> roles
            , int pageNum, int pageSize) {
        List<String> mergeGroups = new ArrayList<>();
        mergeGroups.addAll(groups);
        mergeGroups.addAll(roles);
        return taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .taskCandidateGroupIn(mergeGroups)
                .orderByTaskCreateTime().desc()
                .listPage(pageNum, pageSize);
    }

    /**
     * 获取流程历史中已执行节点
     *
     * @param processId
     * @return
     */
    public static List<HistoricActivityInstance> getHistoricActivityInstance(String processId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(processId)
                .orderByActivityId().asc().list();
    }

    /**
     * 根据任务id获得对应的流程实例
     *
     * @param taskId
     * @return
     * @throws Exception
     */
    public ProcessInstance findProcessInstanceByTaskId(String taskId) throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(findTaskById(taskId).getProcessInstanceId())
                .singleResult();
        if (processInstance == null) {
            throw new Exception("流程实例未找到");
        }
        return processInstance;
    }

    /**
     * 根据流程实例取得该实例信息
     *
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    public static HistoricProcessInstance findProcessInstanceByProcessInstanceId(String processInstanceId) throws Exception {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance == null) {
            throw new Exception("流程实例未找到");
        }
        return processInstance;
    }

    /**
     * 根据任务ID获得任务实例
     *
     * @param taskId
     * @return
     * @throws Exception
     */
    public static TaskEntity findTaskById(String taskId) throws Exception {
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new Exception("任务实例未找到");
        }
        return task;
    }

    /**
     * 根据流程实例id获取当前任务
     *
     * @param processId
     * @return
     * @throws Exception
     */
    public static Task getTaskIdByProcessId(String processId) throws Exception {
        Task task = taskService.createTaskQuery().processInstanceId(processId).active().singleResult();
        if (task == null) {
            throw new Exception("当前任务未找到");
        }
        return task;
    }

    public static void completeTask(String taskId) throws Exception {
        taskService.complete(taskId);
    }

    /**
     * 根据流程实例id获取批注信息
     *
     * @param processId
     * @return
     * @throws Exception
     */
    public static List<Comment> getTaskCommentsByProcessId(String processId) throws Exception {
        List<Comment> comments = taskService.getProcessInstanceComments(processId);
        if (comments == null) {
            throw new Exception("当前批注未找到");
        }
        return comments;
    }

//    public static List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
//        //24小时制
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        //用以保存高亮的线flowId
//        List<String> highFlows = new ArrayList<>();
//        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
//            //对历史流程节点进行遍历
//            //得到节点定义的详细信息
//            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());
//            //用以保存后续开始时间相同的节点
//            List<FlowNode> sameStartTimeNodes = new ArrayList<>();
//            FlowNode sameActivityImpl1 = null;
//            //第一个节点
//            HistoricActivityInstance activityImp1_ = historicActivityInstances.get(i);
//            HistoricActivityInstance activityImp2_;
//            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
//                //后续第一个节点
//                activityImp2_ = historicActivityInstances.get(j);
//
//                //都是usertask,且主节点与后续节点的开始时间相同，说明不是真实的后继节点
//                if (activityImp1_.getActivityType().equals("userTask") && activityImp2_.getActivityType().equals("userTask") &&
//                        df.format(activityImp1_.getStartTime()).equals(df.format(activityImp2_.getStartTime()))) {
//                } else {
//                    //找到紧跟在后面的一个节点
//                    sameActivityImpl1 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(j).getActivityId());
//                    break;
//                }
//            }
//            //将后面第一个节点放在时间相同节点的集合里
//            sameStartTimeNodes.add(sameActivityImpl1);
//            for (int k = i + 1; k < historicActivityInstances.size() - 1; k++) {
//                //后续第一个节点
//                HistoricActivityInstance activityImp1 = historicActivityInstances.get(k);
//                //后续第二个节点
//                HistoricActivityInstance activityImp2 = historicActivityInstances.get(k + 1);
//
//                //如果第一个节点和第二个节点开始时间相同保存
//                if (df.format(activityImp1.getStartTime()).equals(df.format(activityImp2.getStartTime()))) {
//                    FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityImp2.getActivityId());
//                    sameStartTimeNodes.add(sameActivityImpl2);
//                } else {
//                    //有不相同跳出循环
//                    break;
//                }
//            }
//            //取出节点的所有出去的线
//            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows();
//
//            //对所有的线进行遍历
//            for (SequenceFlow pvmTransition : pvmTransitions) {
//                //如果去除的线的目标节点存在时间相同的节点里，保存该线的id,进行高亮显示
//                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(pvmTransition.getTargetRef());
//                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
//                    highFlows.add(pvmTransition.getId());
//                }
//            }
//        }
//        return highFlows;
//    }

    public static List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
        //用以保存高亮的线flowId
        List<String> highFlows = new ArrayList<>();
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            //对历史流程节点进行遍历
            //得到节点定义的详细信息
            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());
            //用以保存后续开始时间相同的节点
            List<FlowNode> sameStartTimeNodes = new ArrayList<>();
            FlowNode sameActivityImpl1 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i + 1).getActivityId());

            //将后面第一个节点放到时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);

            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImp1_ = historicActivityInstances.get(j);//后续第一个节点
                HistoricActivityInstance activityImp2_ = historicActivityInstances.get(j + 1);//后续第二个节点;
                if (activityImp1_.getStartTime().equals(activityImp2_.getStartTime())) {
                    //如果第一个节点和第二个节点开始时间相同保存
                    FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityImp2_.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    //有不相同跳出循坏
                    break;
                }
            }
            //取出节点的所有出去的线
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows();

            //对所有的线进行遍历
            for (SequenceFlow pvmTransition : pvmTransitions) {
                //如果取出的线的目标节点存在时间相同的节点里，保存该线的id,进行高亮显示
                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(pvmTransition.getTargetRef());
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }

    /**
     * 获取json格式的流程图数据
     *
     * @param bpmnModel
     * @return
     */
    public static FlowViewDataDTO getFlowViewData(BpmnModel bpmnModel) {
        FlowViewDataDTO flowViewDataDTO = new FlowViewDataDTO();
        //用于存所有连线信息
        List<SequenceFlowDTO> edges = new ArrayList<>();
        //用于存所有节点信息
        List<FlowNodeDTO> nodes = new ArrayList<>();

        //因为我们这里只定义了一个process，所以获取集合中的第一个即可
        Process process = bpmnModel.getProcesses().get(0);
        //获取所有的FlowElement信息
        Collection<FlowElement> flowElements = process.getFlowElements();
        for (FlowElement flowElement : flowElements) {
            //判断是否是连线
            if (flowElement instanceof SequenceFlow) {
                SequenceFlowDTO sequenceFlowDTO = new SequenceFlowDTO();
                sequenceFlowDTO.setClazz("flow");
                sequenceFlowDTO.setSource(((SequenceFlow) flowElement).getSourceRef());
                sequenceFlowDTO.setTarget(((SequenceFlow) flowElement).getTargetRef());
                sequenceFlowDTO.setLabel(flowElement.getName());
                edges.add(sequenceFlowDTO);
            } else {
                FlowNodeDTO nodeDTO = new FlowNodeDTO();
                nodeDTO.setId(flowElement.getId());
                nodeDTO.setLabel(flowElement.getName());
                //获取对应的属性值
                String clazz = FlowNodeClazzEnum.getClazzByObject(flowElement);
                nodeDTO.setClazz(clazz);
                //根据节点id获取坐标信息
//                GraphicInfo graphicInfo = bpmnModel.getLabelGraphicInfo(flowElement.getId());
//                nodeDTO.setX(graphicInfo.getX());
//                nodeDTO.setY(graphicInfo.getY());
                nodes.add(nodeDTO);
            }
        }
        flowViewDataDTO.setEdges(edges);
        flowViewDataDTO.setNodes(nodes);
        return flowViewDataDTO;
    }

    /**
     * 根据节点属性类，返回前端需要的对应节点属性
     *
     * @param flowElement
     * @return
     */
    public static String getFlowClazz(FlowElement flowElement) {
        if (flowElement instanceof StartEvent) {
            return "start";
        } else if (flowElement instanceof UserTask) {
            return "userTask";
        } else if (flowElement instanceof ScriptTask) {
            return "scriptTask";
        } else if (flowElement instanceof ReceiveTask) {
            return "receiveTask";
        } else if (flowElement instanceof EndEvent) {
            return "end";
        } else if (flowElement instanceof SequenceFlow) {
            return "flow";
        } else {
            return "";
        }
    }

}
