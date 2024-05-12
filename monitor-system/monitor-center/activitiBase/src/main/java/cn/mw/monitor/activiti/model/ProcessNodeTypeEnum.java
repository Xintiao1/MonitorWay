package cn.mw.monitor.activiti.model;

public enum ProcessNodeTypeEnum {
    ServiceTask(0),
    UserTask(1),
    InclusiveGateway(6),
    TaskStart(2),
    Unknown(-1),
    //并行网关
    ParallelGateway(4),
    //单一网关
    ExclusiveGateway(5);

    private int type;

    ProcessNodeTypeEnum(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static ProcessNodeTypeEnum valueOf(int type) {
        ProcessNodeTypeEnum[] values = ProcessNodeTypeEnum.values();

        for (ProcessNodeTypeEnum value : values) {
            if (value.getType() == type) {
                return value;
            }
        }
        return Unknown;
    }
}
