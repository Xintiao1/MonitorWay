package cn.mw.monitor.activiti.dto;

;import org.activiti.bpmn.model.*;

/**
 * @author syt
 * @Date 2020/11/20 16:38
 * @Version 1.0
 */
public enum FlowNodeClazzEnum {
//    case 'start': return 'start-node';
//    case 'end': return 'end-node';
//    case 'gateway': return 'gateway-node';
//    case 'exclusiveGateway': return 'exclusive-gateway-node';
//    case 'parallelGateway': return 'parallel-gateway-node';
//    case 'inclusiveGateway': return 'inclusive-gateway-node';
//    case 'timerStart': return 'timer-start-node';
//    case 'messageStart': return 'message-start-node';
//    case 'signalStart': return 'signal-start-node';
//    case 'userTask': return 'user-task-node';
//    case 'scriptTask': return 'script-task-node';
//    case 'mailTask': return 'mail-task-node';
//    case 'javaTask': return 'java-task-node';
//    case 'receiveTask': return 'receive-task-node';
//    case 'timerCatch': return 'timer-catch-node';
//    case 'messageCatch': return 'message-catch-node';
//    case 'signalCatch': return 'signal-catch-node';
//    case 'subProcess': return 'sub-process-node';
//    default: return 'task-node';
    START(StartEvent.class,"start"),
    END(EndEvent.class,"end"),
    GATEWAY(Gateway.class,"gateway"),
    EXCLUSIVEGATEWAY(ExclusiveGateway.class,"exclusiveGateway"),
    PARALLELGATEWAY(ParallelGateway.class,"'parallelGateway'"),
    TIMEERSTART(TimerEventDefinition.class,"timerStart"),
    USERTASK(UserTask.class,"userTask"),
    SCRIPTTASK(ScriptTask.class,"scriptTask"),
    RECEIVETASK(ReceiveTask.class,"receiveTask"),
    FLOW(FlowElement.class,"flow")
    ;
    private Object object;
    private String clazz;

    public static String getClazzByObject(Object o) {
        for(FlowNodeClazzEnum f : FlowNodeClazzEnum.values()) {
            if(o.getClass() == f.getObject()) {
                return f.getClazz();
            }
        }
        return null;
    }
    FlowNodeClazzEnum() {
    }

    FlowNodeClazzEnum(Object object, String clazz) {
        this.object = object;
        this.clazz = clazz;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
