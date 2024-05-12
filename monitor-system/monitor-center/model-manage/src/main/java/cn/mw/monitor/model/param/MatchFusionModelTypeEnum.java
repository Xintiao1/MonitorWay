package cn.mw.monitor.model.param;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型管理操作类型
 */
public enum MatchFusionModelTypeEnum {
    SUPER_FUSION("超融合","superfusion",272),
    FUSION_CLUSTER("集群","cluster",1007),
    FUSION_HOST("宿主机","host",1008),
    FUSION_VM("虚拟机","vm",1009),
    FUSION_STORAGE("存储设备","storage",1010);

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

    MatchFusionModelTypeEnum(String name, String type, Integer modelId) {
        this.name = name;
        this.type = type;
        this.modelId = modelId;
    }
    public static MatchFusionModelTypeEnum valueOf(Integer modelId){
        for(MatchFusionModelTypeEnum checkModelEnum : MatchFusionModelTypeEnum.values()){
            if(modelId.equals(checkModelEnum.getModelId())){
                return checkModelEnum;
            }
        }
        return null;
    }


    public static MatchFusionModelTypeEnum getTypeOf(String type){
        for(MatchFusionModelTypeEnum checkModelEnum : MatchFusionModelTypeEnum.values()){
            if(type.equals(checkModelEnum.getType())){
                return checkModelEnum;
            }
        }
        return null;
    }
    public static Integer getModelId(String type){
        for(MatchFusionModelTypeEnum checkModelEnum : MatchFusionModelTypeEnum.values()){
            if(type.equals(checkModelEnum.getType())){
                return checkModelEnum.getModelId();
            }
        }
        return null;
    }

    public static List<Integer> getModelIdList(){
        List<Integer> list = new ArrayList<>();
        for(MatchFusionModelTypeEnum checkModelEnum : MatchFusionModelTypeEnum.values()){
            list.add(checkModelEnum.getModelId());
        }
        return list;
    }
}
