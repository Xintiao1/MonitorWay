package cn.mw.monitor.graph.modelAsset;

public class ModelGroupParam {
    private Integer ownModelId;
    private Integer groupId;

    public ModelGroupParam(Integer ownModelId ,Integer groupId){
        this.ownModelId = ownModelId;
        this.groupId = groupId;
    }

    public Integer getOwnModelId() {
        return ownModelId;
    }

    public void setOwnModelId(Integer ownModelId) {
        this.ownModelId = ownModelId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
}
