package cn.mw.monitor.model.param;

/**
 * 模型管理操作类型
 */
public enum MatchModelTypeEnum {
    RANCHER("Rancher","rancher",22),
    CLUSTER("Cluster","cluster",501),
    NODES("Nodes","node",502),
    PROJECTS("Projects","project",503),
    NAMESPACE("NameSpace","namespace",504);
    private String name;
    private String type;
    private Integer modelId;

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    MatchModelTypeEnum(String name,String type, Integer modelId) {
        this.name = name;
        this.type = type;
        this.modelId = modelId;
    }
    public static MatchModelTypeEnum valueOf(Integer modelId){
        for(MatchModelTypeEnum checkModelEnum : MatchModelTypeEnum.values()){
            if(modelId.equals(checkModelEnum.getModelId())){
                return checkModelEnum;
            }
        }
        return null;
    }

    public static MatchModelTypeEnum getTypeOf(String type){
        for(MatchModelTypeEnum checkModelEnum : MatchModelTypeEnum.values()){
            if(type.equals(checkModelEnum.getType())){
                return checkModelEnum;
            }
        }
        return null;
    }
    public static Integer getModelId(String type){
        for(MatchModelTypeEnum checkModelEnum : MatchModelTypeEnum.values()){
            if(type.equals(checkModelEnum.getType())){
                return checkModelEnum.getModelId();
            }
        }
        return null;
    }
}
