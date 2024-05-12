package cn.mw.monitor.service.graph;

import lombok.Data;

import java.util.Map;
import java.util.Objects;

@Data
public class NodeParam {
    public static final String EMPTY_LABEL = "无实例";
    private static final String SEP = "_";
    private String id;
    private Integer level;
    private Integer comboId;
    private String label;
    private Integer realId;

    public NodeParam(){

    }

    public NodeParam(String id){
        this.id = id;
        String[] values = id.split(SEP);
        this.comboId = Integer.parseInt(values[0]);
        this.realId = Integer.parseInt(values[1]);
    }

    public NodeParam(Integer comboId ,Integer realId){
        this.realId = realId;
        this.comboId = comboId;
        this.id = comboId + SEP + this.realId;
    }

    public void extractInfoFrom(Map<Integer ,InstanceModelMapper> instanceMap){
        if(null == this.level){
            this.level = 1;
        }

        InstanceModelMapper instanceModelMapper = instanceMap.get(this.realId);
        if(null != instanceModelMapper){
            this.comboId = instanceModelMapper.getModelId();
            this.id = instanceModelMapper.getModelId() + SEP + this.realId;
            this.label = instanceModelMapper.getInstanceName();
        }
    }

    public void initEmptyNode(Integer modelId){
        this.label = EMPTY_LABEL;
        this.realId = 0;
        this.comboId = modelId;
        this.level = 1;
        this.id = modelId + SEP + this.realId;
    }

    public boolean isRootNode(){
        return this.level == 0;
    }

    public boolean isEmptyNode(){
        return this.realId.equals(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeParam nodeParam = (NodeParam) o;
        return Objects.equals(id, nodeParam.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
