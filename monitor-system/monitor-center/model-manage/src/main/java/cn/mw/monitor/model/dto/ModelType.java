package cn.mw.monitor.model.dto;

/**
 * @author xhy
 * @date 2021/2/5 15:22
 */
public enum ModelType {
    COMMON_MODEL(1, "普通模型"),
    FATHER_MODEL(2, "父模型"),
    SON_MODEL(3, "子模型");

    private Integer typeId;
    private String typeName;

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    ModelType(Integer typeId, String typeName) {
        this.typeName = typeName;
        this.typeId = typeId;
    }

    public static ModelType valueOf(Integer type){
        for(ModelType modelType : ModelType.values()){
            if(type.equals(modelType.getTypeId())){
                return modelType;
            }
        }
        return null;
    }
}
