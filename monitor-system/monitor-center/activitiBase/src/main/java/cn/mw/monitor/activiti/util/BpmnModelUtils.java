package cn.mw.monitor.activiti.util;

import cn.mw.monitor.activiti.model.ProcessNodeTypeEnum;
import cn.mw.monitor.activiti.model.ProcessView;
import cn.mw.monitor.activiti.param.*;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BpmnModelUtils {

    private static final String PROCESS_START = "流程开始";

    private static Map<String,List<Integer>> notifier = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/activiti/service/impl/ActivitiServiceImpl");
    public static  void addNotifier(String taskid,List<Integer> notifierUser){
       notifier.put(taskid,notifierUser);
    }

    public static  Map<String,List<Integer>>  getNotifier(){
        return notifier;
    }



    public static  void celanNotifier(){
       notifier.clear();
    }


    public static BpmnModel createProcess(ProcessParam processParam) {

        BpmnModel bpmnModel = null;
        if (null != processParam.getProcessDefinition() && null != processParam.getProcessDefinition().getChildNode()) {
            //实例化BpmnModel对象
            bpmnModel = new BpmnModel();
            //开始节点的属性
            String id = UuidUtil.nextUuid();
            String startEventId = "start-" + id;
            StartEvent startEvent = new StartEvent();
            startEvent.setId(startEventId);
            startEvent.setName(startEventId);

            //结束节点属性
            EndEvent endEvent = new EndEvent();
            String endEventId = "end-" + id;
            endEvent.setId(endEventId);
            endEvent.setName(endEventId);

            //Process对象
            Process process = new Process();
            process.setId("process-" + id);
            process.setName(processParam.getProcessName());
            process.addFlowElement(startEvent);

            FlowNode flowNode = analyseChild(startEvent, processParam.getProcessDefinition().getChildNode(), process);

            SequenceFlow sequenceFlow = genSequenceFlow(flowNode, endEvent);
            List<SequenceFlow> sequenceFlows = new ArrayList<SequenceFlow>();
            sequenceFlows.add(sequenceFlow);
            process.addFlowElement(sequenceFlow);

            endEvent.setIncomingFlows(sequenceFlows);
            process.addFlowElement(endEvent);
            bpmnModel.addProcess(process);
        }

        return bpmnModel;
    }

    private static FlowNode analyseChild(FlowNode flowNode, ProcessNode processNode, Process process) {
        //获取节点类型
        int type = processNode.getNodeInfo().getInfoType();
        List<SequenceFlow> sequenceFlows = new ArrayList<SequenceFlow>();
        SequenceFlow sequenceFlow = null;
        FlowNode ret = null;
        ProcessNodeTypeEnum processNodeType = ProcessNodeTypeEnum.valueOf(type);
        switch (processNodeType) {
            case UserTask:
                UserTask userTask = genUserTask(type, processNode, process);
                setTask(flowNode, userTask, process);
                if (null != processNode.getChildNode()) {
                    analyseChild(userTask, processNode.getChildNode(), process);
                }
                ret = userTask;
                break;
            case TaskStart:
                UserTask userTaskTwo = genUserTask(type, processNode, process);
                setTask(flowNode, userTaskTwo, process);
                if (null != processNode.getChildNode()) {
                    analyseChild(userTaskTwo, processNode.getChildNode(), process);
                }
                ret = userTaskTwo;
                break;
            case ServiceTask:
                ServiceTask serviceTask = genServiceTask(type, processNode, process);
                setTask(flowNode, serviceTask, process);
                if (null != processNode.getChildNode()) {
                    analyseChild(serviceTask, processNode.getChildNode(), process);
                }
                ret = serviceTask;
                break;

            case InclusiveGateway:
                InclusiveGateway inclusiveGateway = genInclusiveGateway(process);
                sequenceFlow = genSequenceFlow(flowNode, inclusiveGateway);
                sequenceFlows.add(sequenceFlow);
                inclusiveGateway.setIncomingFlows(sequenceFlows);
                ret = inclusiveGateway;
        }

        return ret;
    }

    private static FlowNode analyseChilds(FlowNode flowNode, ProcessNode processNode, Process process, EndEvent endEvent) {
        FlowNode ret = null;

        analyseChildNew1(flowNode, processNode, process, endEvent);
        return ret;
    }

    private static void analyseChildNew1(FlowNode flowNode, ProcessNode childNote, Process process, EndEvent endEvent) {
        int type = childNote.getNodeInfo().getInfoType();
        ProcessNodeTypeEnum processNodeType = ProcessNodeTypeEnum.valueOf(type);
        FlowNode gatewayOrTask = getProcessType(childNote, process);
        setGateway(childNote, flowNode, gatewayOrTask, process);
        switch (processNodeType) {
            //判断是否为网关
            case ParallelGateway:
            case ExclusiveGateway:
                //使用网关画线
                //FlowNode childTask = getProcessType(childNote.getChildNode(),process);
                for (ProcessNode node : childNote.getConditionNodes()) {
                    //下个节点
                    if (node.getChildNode() != null) {
                        FlowNode task = getProcessType(node.getChildNode(), process);
                        setGateway(node, gatewayOrTask, task, process);
                        analyseChildNew1(task, node.getChildNode(), process, endEvent);
                    } else {
                        //setGateway(node, gatewayOrTask, childTask, process);
                        analyseChildNew1(gatewayOrTask, childNote.getChildNode(), process, endEvent);
                    }
                }
                break;
            default:
                //不是网关
                if (childNote.getChildNode() != null) {
                    analyseChildNew1(gatewayOrTask, childNote.getChildNode(), process, endEvent);
                }
                break;
        }
        if (childNote.getChildNode() == null) {
            SequenceFlow sequenceFlow = genSequenceFlow(childNote, gatewayOrTask, endEvent);
            List<SequenceFlow> sequenceFlows = new ArrayList<>();
            sequenceFlows.add(sequenceFlow);
            process.addFlowElement(sequenceFlow);
            endEvent.setIncomingFlows(sequenceFlows);
        }

    }

    private static void gatewayChild(FlowNode gateway, FlowNode child, ProcessNode childNote, Process process, EndEvent endEvent) {
        SequenceFlow sequenceFlow = genSequenceFlow(childNote, gateway, child);
        List<SequenceFlow> sequenceFlows = new ArrayList<>();
        sequenceFlows.add(sequenceFlow);
        process.addFlowElement(sequenceFlow);
        endEvent.setIncomingFlows(sequenceFlows);

        analyseChildNew1(gateway, childNote.getChildNode(), process, endEvent);
    }

    private static FlowNode getProcessType(ProcessNode node, Process process) {
        int type = node.getNodeInfo().getInfoType();
        ProcessNodeTypeEnum processNodeType = ProcessNodeTypeEnum.valueOf(type);
        FlowNode tempFlowNode = null;
        switch (processNodeType) {
            case UserTask:
            case TaskStart:
                tempFlowNode = genUserTask(type, node, process);
                break;
            case ServiceTask:
                tempFlowNode = genServiceTask(type, node, process);
                break;
            case ExclusiveGateway:
                tempFlowNode = genExclusiveGateway(node, process);
                break;
            case ParallelGateway:
                tempFlowNode = genParallelGateway(node, process);
                break;
            case InclusiveGateway:
                tempFlowNode = genInclusiveGateway(process);
                break;
        }
        return tempFlowNode;
    }

    private static SequenceFlow genSequenceFlow1(ProcessNode node, FlowNode startFlowNode, FlowNode endFlowNode) {
        SequenceFlow sequenceFlow = new SequenceFlow();
        String id = startFlowNode.getName() + UuidUtil.nextUuid();
        sequenceFlow.setId(id);
        sequenceFlow.setName(id);
        if (node != null && StringUtils.isNotEmpty(node.getCondition())) {
            sequenceFlow.setConditionExpression(node.getCondition());
        }
        sequenceFlow.setSourceRef(startFlowNode.getId());
        sequenceFlow.setTargetRef(endFlowNode.getId());
        return sequenceFlow;
    }


    private static void analyseChildNew(FlowNode flowNode, List<ProcessNode> processNode, Process process, EndEvent endEvent) {

        for (ProcessNode node : processNode) {
            int type = node.getNodeInfo().getInfoType();
            List<SequenceFlow> sequenceFlows = new ArrayList<SequenceFlow>();
            SequenceFlow sequenceFlow = null;
            ProcessNodeTypeEnum processNodeType = ProcessNodeTypeEnum.valueOf(3);
            FlowNode tempFlowNode = null;
            switch (processNodeType) {
                case UserTask:
                case TaskStart:
                    tempFlowNode = genUserTask(type, node, process);
                    break;
                case ServiceTask:
                    tempFlowNode = genServiceTask(type, node, process);
                    break;
                case ExclusiveGateway:
                    tempFlowNode = genExclusiveGateway(node, process);
                    break;
                case InclusiveGateway:
                    tempFlowNode = genInclusiveGateway(process);
                    break;
            }
            setGateway(node, flowNode, tempFlowNode, process);
            if (CollectionUtils.isNotEmpty(node.getChildNodes())) {
                for (ProcessNode nodeChild : node.getChildNodes()) {
                    SequenceFlow temp = getSequenceFlow(tempFlowNode, nodeChild, process);
                    process.addFlowElement(temp);
                    tempFlowNode.setIncomingFlows(sequenceFlows);
                    if (CollectionUtils.isNotEmpty(nodeChild.getChildNodes())) {
                        analyseChildNew(tempFlowNode, nodeChild.getChildNodes(), process, endEvent);
                    }
                }
                process.addFlowElement(tempFlowNode);

            } else {
                sequenceFlow = genSequenceFlow(null, tempFlowNode, endEvent);
                sequenceFlows.add(sequenceFlow);
                process.addFlowElement(sequenceFlow);
                endEvent.setIncomingFlows(sequenceFlows);
            }
        }

    }

    private static SequenceFlow getSequenceFlow(FlowNode flowNode, ProcessNode processNode, Process process) {
        SequenceFlow sequenceFlow = new SequenceFlow();
        if (StringUtils.isNotEmpty(processNode.getCondition())) {
            sequenceFlow.setConditionExpression(processNode.getCondition());
        }
        sequenceFlow.setSourceRef(flowNode.getId());
        String id = process.getName() + UuidUtil.nextUuid();
        sequenceFlow.setTargetRef(id);
        return sequenceFlow;

    }

    private static ExclusiveGateway genExclusiveGateway(ProcessNode processNode, Process process) {
        String id = process.getName() + UuidUtil.nextUuid();
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);
        exclusiveGateway.setName(processNode.getNodeName());
        return exclusiveGateway;
    }

    private static ParallelGateway genParallelGateway(ProcessNode processNode, Process process) {
        String id = process.getName() + UuidUtil.nextUuid();
        ParallelGateway parallelGateway = new ParallelGateway();
        parallelGateway.setId(id);
        parallelGateway.setName(processNode.getNodeName());
        return parallelGateway;
    }

    private static void setGateway(ProcessNode node, FlowNode startFlowNode, FlowNode endFlowNode, Process process) {
        List<SequenceFlow> sequenceFlows = new ArrayList<SequenceFlow>();
        SequenceFlow sequenceFlow = genSequenceFlow(node, startFlowNode, endFlowNode);
        sequenceFlows.add(sequenceFlow);
        process.addFlowElement(sequenceFlow);
        endFlowNode.setIncomingFlows(sequenceFlows);
        process.addFlowElement(endFlowNode);
    }


    private static SequenceFlow genSequenceFlow(ProcessNode node, FlowNode startFlowNode, FlowNode endFlowNode) {
        SequenceFlow sequenceFlow = new SequenceFlow();
        String id = startFlowNode.getName() + UuidUtil.nextUuid();
        sequenceFlow.setId(id);
        sequenceFlow.setName(id);
        if (node != null && StringUtils.isNotEmpty(node.getCondition())) {
            sequenceFlow.setConditionExpression(node.getCondition());
        }
        sequenceFlow.setSourceRef(startFlowNode.getId());
        sequenceFlow.setTargetRef(endFlowNode.getId());
        return sequenceFlow;
    }

    private static void setTask(FlowNode flowNode, Task task, Process process) {
        List<SequenceFlow> sequenceFlows = new ArrayList<SequenceFlow>();
        SequenceFlow sequenceFlow = genSequenceFlow(flowNode, task);
        sequenceFlows.add(sequenceFlow);
        process.addFlowElement(sequenceFlow);
        task.setIncomingFlows(sequenceFlows);
        process.addFlowElement(task);
    }

    private static SequenceFlow genSequenceFlow(FlowNode startFlowNode, FlowNode endFlowNode) {
        SequenceFlow sequenceFlow = new SequenceFlow();
        String id = startFlowNode.getName() + "-" + endFlowNode.getName();
        sequenceFlow.setId(id);
        sequenceFlow.setName(id);
        sequenceFlow.setSourceRef(startFlowNode.getId());
        sequenceFlow.setTargetRef(endFlowNode.getId());
        return sequenceFlow;
    }

    private static InclusiveGateway genInclusiveGateway(Process process) {
        String id = process.getName() + UuidUtil.nextUuid();
        InclusiveGateway inclusiveGateway = new InclusiveGateway();
        inclusiveGateway.setId(id);
        return inclusiveGateway;
    }

    private static UserTask genUserTask(Integer type, ProcessNode processNode, Process process) {
        UserTask userTask = new UserTask();
        String id = genId(type, processNode);
        userTask.setId(id);
        userTask.setName(processNode.getNodeName());
        //设置审批类型
        List<String> group = new ArrayList<>();
        //当流程类型为拦截操作时候
   /*    if (processNode.getNodeInfo().getInfoType()==2){

       }else {*/
        switch (processNode.getNodeInfo().getNodeType()) {
            case 2:
                for (int i = 0; i < processNode.getNodeInfo().getRole().size(); i++) {
                    group.add("ROLE-" + processNode.getNodeInfo().getRole().get(i).toString());
                }
                userTask.setCandidateGroups(group);
                break;
            case 1:
                for (int i = 0; i < processNode.getNodeInfo().getGroup().size(); i++) {
                    group.add("GROUP-" + processNode.getNodeInfo().getGroup().get(i).toString());
                }
                userTask.setCandidateGroups(group);
                break;
            case 0:
                for (int i = 0; i < processNode.getNodeInfo().getPeople().size(); i++) {
                    group.add("USER-" + processNode.getNodeInfo().getPeople().get(i).toString());
                }
                userTask.setCandidateUsers(group);
                break;
            default:
        }
        /*     }*/


        return userTask;
    }

    //根据类型返回节点id
    private static String genId(Integer type, ProcessNode processNode) {
        switch (ProcessNodeTypeEnum.valueOf(type)) {
            case UserTask:
                return "utilProcess" + UuidUtil.nextUuid();
            case TaskStart:
                return "useProcess" + UuidUtil.nextUuid();
            default:
                return "otherProcess" + UuidUtil.nextUuid();
        }
    }

    private static ServiceTask genServiceTask(Integer type, ProcessNode processNode, Process process) {
        ServiceTask serviceTask = new ServiceTask();
        String id = genId(type, processNode);
        serviceTask.setId(id);
        serviceTask.setName(id);
        serviceTask.setImplementation("cn.mw.monitor.activiti.task.ModelCallBackTask");
        serviceTask.setImplementationType("class");
        return serviceTask;
    }

    private static void exctractChildProcessNode(ProcessNode parentProcessNode, String parentNodeId
            , Map<String, List<SequenceFlow>> sequenceFlowMap, Map<String, FlowElement> flowElementMap) {
        List<SequenceFlow> childSequenceFlowList = sequenceFlowMap.get(parentNodeId);

        if (null == childSequenceFlowList || childSequenceFlowList.size() == 0) {
            return;
        }

        for (SequenceFlow sequenceFlow : childSequenceFlowList) {
            FlowElement flowElement = flowElementMap.get(sequenceFlow.getTargetRef());
            if (null != flowElement) {
                ProcessNode childProcessNode = new ProcessNode();
                childProcessNode.setNodeName(flowElement.getName());
                if (flowElement instanceof UserTask) {
                    childProcessNode.setType(ProcessNodeTypeEnum.UserTask.getType());
                    parentProcessNode.setChildNode(childProcessNode);
                }

                if (flowElement instanceof InclusiveGateway) {
                    childProcessNode.setType(ProcessNodeTypeEnum.InclusiveGateway.getType());
                }
                exctractChildProcessNode(childProcessNode, flowElement.getId(), sequenceFlowMap, flowElementMap);
            }

        }
    }

    public static List<String> getTaskCandidateGroup() {
        ILoginCacheInfo iLoginCacheInfo = (ILoginCacheInfo) SpringUtils.getBean("iLoginCacheInfo");
        MwRoleDTO roleDTO = iLoginCacheInfo.getRoleInfo();
        List<String> taskCandidateGroup = new ArrayList<>();
        taskCandidateGroup.add(roleDTO.getRoleId().toString());

        LoginContext loginContext = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName());
        List<Integer> groupList = loginContext.getLoginInfo().getUser().getUserGroup();
        if (null != groupList) {
            groupList.forEach(data -> {
                taskCandidateGroup.add(data.toString());
            });
        }
        return taskCandidateGroup;
    }

    public static BpmnModel createnewProcess(ProcessParam processParam) {
        BpmnModel bpmnModel = null;
        if (null != processParam.getProcessDefinition() && null != processParam.getProcessDefinition().getChildNode()) {
            //实例化BpmnModel对象
            bpmnModel = new BpmnModel();
            //开始节点的属性
            String id = UuidUtil.nextUuid();
            String startEventId = "start-" + id;
            StartEvent startEvent = new StartEvent();
            startEvent.setId(startEventId);
            startEvent.setName(startEventId);

            //结束节点属性
            EndEvent endEvent = new EndEvent();
            String endEventId = "end-" + id;
            endEvent.setId(endEventId);
            endEvent.setName(endEventId);

            //Process对象
            Process process = new Process();
            process.setId("process-" + id);
            process.setName(processParam.getProcessName());


            //过滤所有节点
            List<ProcessNodeHaveful> havefuls = getNodeChild(processParam.getProcessDefinition().getChildNode(), processParam.getCustomIdPath());
            //生成所有连线
            List<SequenceFlow> sequenceFlows = groupSource(startEventId, endEventId, processParam.getCustomIdPath(), havefuls);


            for (ProcessNodeHaveful haveful : havefuls) {
                switch (haveful.getType()) {
                    case 0:
                        /*UserTask userTask3 = genUserTasktwo(haveful);
                        process.addFlowElement(userTask3);*/
                        /* processNodeHaveful = getUseFul(childNode,processNodeHaveful);*/
                        break;
                    case 1:
                        UserTask userTask = genUserTasktwo(haveful);
                        process.addFlowElement(userTask);
                        /* processNodeHaveful = getUseFul(childNode,processNodeHaveful);*/
                        break;
                    case 2:
                        UserTask userTask2 = genUserTasktwo(haveful);
                        process.addFlowElement(userTask2);
                       /* processNodeHaveful = getModel(
                       ,processNodeHaveful);*/
                        break;
                    case 3:
                        /* processNodeHaveful = getCandidate(childNode,processNodeHaveful);*/
                        break;
                    case 4:
                        InclusiveGateway inclusiveGateway = genInclusiveGatewayTwo(haveful);
                        process.addFlowElement(inclusiveGateway);
                        /* processNodeHaveful = getaway(childNode,processNodeHaveful);*/
                        break;
                    case 5:
                        ExclusiveGateway exclusiveGateway = genExclusiveGatewayTwo(haveful);
                        process.addFlowElement(exclusiveGateway);
                        /*processNodeHaveful = getaway(childNode,processNodeHaveful);*/
                        break;
                }
            }
            process.addFlowElement(endEvent);
            process.addFlowElement(startEvent);
            //加先逻辑
            sequenceFlows.stream().forEach(e -> {
                process.addFlowElement(e);
            });
        /*    //根据所出节点及关系画图
            FlowNode flowNode = analyseChild(startEvent, processParam.getProcessDefinition().getChildNode(), process);


            SequenceFlow sequenceFlow = genSequenceFlow(flowNode, endEvent);
            List<SequenceFlow> sequenceFlows=new ArrayList<SequenceFlow>();
            sequenceFlows.add(sequenceFlow);
            process.addFlowElement(sequenceFlow);

            endEvent.setIncomingFlows(sequenceFlows);
            process.addFlowElement(endEvent);*/
            bpmnModel.addProcess(process);
        }

        return bpmnModel;
    }

    private static InclusiveGateway genInclusiveGatewayTwo(ProcessNodeHaveful haveful) {
        InclusiveGateway inclusiveGateway = new InclusiveGateway();
        inclusiveGateway.setId(haveful.getNodeId());
        return inclusiveGateway;
    }

    private static ExclusiveGateway genExclusiveGatewayTwo(ProcessNodeHaveful haveful) {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(haveful.getNodeId());
        return exclusiveGateway;
    }

    private static UserTask genUserTasktwo(ProcessNodeHaveful haveful) {
        UserTask userTask = new UserTask();
        String id = genId(haveful.getType(), null);
        userTask.setId(haveful.getNodeId());
        userTask.setName(haveful.getNodeName());
        //设置审批类型
        List<String> group = new ArrayList<>();
        //当流程类型为拦截操作时候
   /*    if (processNode.getNodeInfo().getInfoType()==2){

       }else {*/
        switch (haveful.getNodeType()) {
            case 3:
                for (int i = 0; i < haveful.getPeople().size(); i++) {
                    group.add("USER-" + haveful.getPeople().get(i).toString());
                    group.add("USER_VARIABLE");
                }
                userTask.setCandidateUsers(group);
                break;
            case 2:
                for (int i = 0; i < haveful.getRole().size(); i++) {
                    group.add("ROLE-" + haveful.getRole().get(i).toString());
                }
                userTask.setCandidateGroups(group);
                break;
            case 1:
                for (int i = 0; i < haveful.getGroup().size(); i++) {
                    group.add("GROUP-" + haveful.getGroup().get(i).toString());
                }
                userTask.setCandidateGroups(group);
                break;
            case 0:
                for (int i = 0; i < haveful.getPeople().size(); i++) {
                    group.add("USER-" + haveful.getPeople().get(i).toString());
                }
                userTask.setCandidateUsers(group);
                break;
            default:
        }
        /*     }*/


        return userTask;
    }

    private static List<SequenceFlow> groupSource(String startEventId, String endEventId, List<List<Integer>> customIdPaths, List<ProcessNodeHaveful> havefuls) {
        List<SequenceFlow> sequenceFlows = new ArrayList<SequenceFlow>();
        List<SequenceFlow> sequenceFlowsfinal = new ArrayList<SequenceFlow>();
        List<String> contion = new ArrayList<>();
        for (List<Integer> customIdPath : customIdPaths) {
            SequenceFlow sequenceFlow = new SequenceFlow();
            sequenceFlow.setId("line_" + UuidUtil.nextUuid());
            for (ProcessNodeHaveful haveful : havefuls) {
                if (customIdPath.get(0).equals(-1)) {
                    sequenceFlow.setSourceRef(startEventId);
                }
                if (customIdPath.get(1).equals(-2)) {
                    sequenceFlow.setTargetRef(endEventId);
                }
                if (haveful.getType() != 3) {
                    if (haveful.getCustomId().equals(customIdPath.get(0))) {
                        sequenceFlow.setSourceRef(haveful.getNodeId());
                    }
                    if (haveful.getCustomId().equals(customIdPath.get(1))) {
                        sequenceFlow.setTargetRef(haveful.getNodeId());
                    }
                }
                if (haveful.getType() == 3) {
                    if (haveful.getCustomId().equals(customIdPath.get(0))) {
                        contion.add(haveful.getNodeId());
                        sequenceFlow.setSourceRef(haveful.getNodeId());
                        sequenceFlow.setConditionExpression(haveful.getText());
                    }
                    if (haveful.getCustomId().equals(customIdPath.get(1))) {
                        sequenceFlow.setTargetRef(haveful.getNodeId());
                    }
                }
            }
            sequenceFlows.add(sequenceFlow);
        }

        for (SequenceFlow flow : sequenceFlows) {
            if (contion.contains(flow.getSourceRef()) || contion.contains(flow.getTargetRef())) {
                if (contion.contains(flow.getSourceRef())) {
                    String lineid = flow.getSourceRef();
                    for (SequenceFlow sequenceFlow : sequenceFlows) {
                        if (lineid.equals(sequenceFlow.getTargetRef())) {
                            flow.setSourceRef(sequenceFlow.getSourceRef());
                        }
                    }
                    sequenceFlowsfinal.add(flow);
                }
            } else {
                sequenceFlowsfinal.add(flow);
            }
        }


        return sequenceFlowsfinal;
    }

    public static List<ProcessNodeHaveful> getNodeChild(ProcessNode childNode, List<List<Integer>> modelPath) {
        List<ProcessNodeHaveful> processNodeHavefuls = new ArrayList<>();

        //z装节点信息//并把条件放到节点里
        ProcessNodeHaveful processNodeHaveful = new ProcessNodeHaveful();
        processNodeHaveful.setType(JudgeType(childNode));

        //用来判断流程走向哪里
        processNodeHaveful.setNodeId("node_" + UuidUtil.nextUuid() + "cut" + childNode.getCustomId() + "cut");
        processNodeHaveful.setCustomId(childNode.getCustomId());
        processNodeHaveful.setNodeName(childNode.getNodeName());
        logger.info("发布上了啊");
        if (childNode.getNodeInfo().getNotifier()!=null&&childNode.getNodeInfo().getNotifier().size()>0){
            addNotifier(processNodeHaveful.getNodeId(),childNode.getNodeInfo().getNotifier());
        }
        switch (processNodeHaveful.getType()) {
            case 0:
                processNodeHaveful = getUseFul(childNode, processNodeHaveful);
                break;
            case 1:
                processNodeHaveful = getUseFul(childNode, processNodeHaveful);
                break;
            case 2:
                processNodeHaveful = getModel(childNode, processNodeHaveful);
                break;
            case 3:
                processNodeHaveful = getCandidate(childNode, processNodeHaveful);
                break;
            case 4:
                processNodeHaveful = getaway(childNode, processNodeHaveful);
                break;
            case 5:
                processNodeHaveful = getaway(childNode, processNodeHaveful);
                break;
        }


        if (childNode.getCustomId() != -2) {
            //组装线路径
            processNodeHavefuls.add(processNodeHaveful);
        }
        if (childNode.getConditionNodes() != null) {
            for (ProcessNode processNode : childNode.getConditionNodes()) {
                processNode.setType(3);
                processNodeHavefuls.addAll(getNodeChild(processNode, modelPath));
            }
        }
        if (childNode.getChildNode() != null) {
            processNodeHavefuls.addAll(getNodeChild(childNode.getChildNode(), modelPath));

        }

        return processNodeHavefuls;
    }

    private static ProcessNodeHaveful getaway(ProcessNode childNode, ProcessNodeHaveful processNodeHaveful) {
        return processNodeHaveful;
    }

    private static ProcessNodeHaveful getCandidate(ProcessNode childNode, ProcessNodeHaveful processNodeHaveful) {
        processNodeHaveful.setText(childNode.getCondition());
        return processNodeHaveful;
    }

    private static ProcessNodeHaveful getModel(ProcessNode childNode, ProcessNodeHaveful processNodeHaveful) {
        processNodeHaveful.setModelId(childNode.getNodeInfo().getModelId());
        processNodeHaveful.setNodeType(childNode.getNodeInfo().getNodeType());
        processNodeHaveful.setRole(childNode.getNodeInfo().getRole());
        processNodeHaveful.setGroup(childNode.getNodeInfo().getGroup());
        processNodeHaveful.setPeople(childNode.getNodeInfo().getPeople());
        return processNodeHaveful;
    }

    private static ProcessNodeHaveful getUseFul(ProcessNode childNode, ProcessNodeHaveful processNodeHaveful) {

        processNodeHaveful.setNodeType(childNode.getNodeInfo().getNodeType());
        processNodeHaveful.setRole(childNode.getNodeInfo().getRole());
        processNodeHaveful.setGroup(childNode.getNodeInfo().getGroup());
        processNodeHaveful.setPeople(childNode.getNodeInfo().getPeople());
        return processNodeHaveful;
    }

    //更具node信息判断
    private static Integer JudgeType(ProcessNode childNode) {
        if (childNode.getNodeInfo().getInfoType() != null) {
            return childNode.getNodeInfo().getInfoType();
        } else {
            return childNode.getNodeInfo().getType();
        }
    }
}
