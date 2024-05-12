package cn.mw.monitor.service.virtual.dto;

/**
 * @author qzg
 * @date 2022/8/30
 */
public enum VirtualizationType {
    VCENTER("VCenter","VCenter",25),
    DATACNETER("Datacenter","数据中心",1014),
    CLUSTER("ClusterComputeResource","集群",1015),
    DATASTORE("Datastore","数据存储",1018),
    HOSTSYSTEM("HostSystem","宿主机",1016),
    VIRTUALMACHINE("VirtualMachine","虚拟机",1017);

    private String type;
    private String desc;
    private Integer modelId;

    VirtualizationType(String type,String desc,Integer modelId){
        this.desc = desc;
        this.type = type;
        this.modelId = modelId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public static String getType(Integer modelId){
        for(VirtualizationType checkModelEnum : VirtualizationType.values()){
            if(modelId.equals(checkModelEnum.getModelId())){
                return checkModelEnum.getType();
            }
        }
        return null;
    }

    public static Integer getModelId(String type){
        for(VirtualizationType checkModelEnum : VirtualizationType.values()){
            if(type.equals(checkModelEnum.getType())){
                return checkModelEnum.getModelId();
            }
        }
        return null;
    }
}
