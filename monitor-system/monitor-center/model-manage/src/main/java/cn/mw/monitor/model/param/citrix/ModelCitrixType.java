package cn.mw.monitor.model.param.citrix;

import cn.mw.monitor.model.param.MatchModelTypeEnum;

/**
 * @author qzg
 * @date 2022/10/11
 */
public enum ModelCitrixType {
    LB_VIRTUAL_SERVERS("LB Virtual Servers","lbvserver",1019),
    LB_SERVER("LB Server","server",1021),
    LB_SERVICES("LB Services","service",1020),
    LOAD_BALANCING("Load Balancing","LB",1027),
    GSLB("GSLB","GSLB",1028),
    GSLB_VIRTUAL_SERVERS("GSLB Virtual Servers","gslbvserver",1022),
    GSLB_SERVICES("GSLB Services","gslbservice",1023),
    GSLB_DOMAIN("GSLB Domain","gslbdomain",1024);

    private String type;
    private String desc;
    private Integer modelId;

    ModelCitrixType(String type,String desc,Integer modelId){
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

    public static String getDesc(Integer modelId){
        for(ModelCitrixType checkModelEnum : ModelCitrixType.values()){
            if(modelId.intValue() == checkModelEnum.getModelId().intValue()){
                return checkModelEnum.getDesc();
            }
        }
        return null;
    }
}
