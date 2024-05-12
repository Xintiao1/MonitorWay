package cn.mw.monitor.model.param;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型管理操作类型
 */
public enum PrometheusContainerEnum {
    PROMETHEUS_CLUSTER("集群","Cluster",505),
    PROMETHEUS_NAMESPACE("命名空间","NameSpace",506),
    PROMETHEUS_POD("节点","Pod",507),
    PROMETHEUS_CONTAINER("容器","Container",508);

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

    PrometheusContainerEnum(String name, String type, Integer modelId) {
        this.name = name;
        this.type = type;
        this.modelId = modelId;
    }
    public static PrometheusContainerEnum valueOf(Integer modelId){
        for(PrometheusContainerEnum checkModelEnum : PrometheusContainerEnum.values()){
            if(modelId.equals(checkModelEnum.getModelId())){
                return checkModelEnum;
            }
        }
        return null;
    }


    public static PrometheusContainerEnum getTypeOf(String type){
        for(PrometheusContainerEnum checkModelEnum : PrometheusContainerEnum.values()){
            if(type.equals(checkModelEnum.getType())){
                return checkModelEnum;
            }
        }
        return null;
    }
    public static Integer getModelId(String type){
        for(PrometheusContainerEnum checkModelEnum : PrometheusContainerEnum.values()){
            if(type.equals(checkModelEnum.getType())){
                return checkModelEnum.getModelId();
            }
        }
        return null;
    }

    public static String getType(Integer modelId){
        for(PrometheusContainerEnum checkModelEnum : PrometheusContainerEnum.values()){
            if(checkModelEnum.getModelId().equals(modelId)){
                return checkModelEnum.getType();
            }
        }
        return null;
    }

    public static List<Integer> getModelIdList(){
        List<Integer> list = new ArrayList<>();
        for(PrometheusContainerEnum checkModelEnum : PrometheusContainerEnum.values()){
            list.add(checkModelEnum.getModelId());
        }
        return list;
    }
}
