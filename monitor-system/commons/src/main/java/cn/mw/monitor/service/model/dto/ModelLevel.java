package cn.mw.monitor.service.model.dto;

public enum ModelLevel {
    BuildIn(0 ,"内置"), UserDefine(1,"自定义");
    private int level;
    private String desc;

    ModelLevel(int level ,String desc){
        this.level = level;
        this.desc = desc;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static ModelLevel valueOf(int level){
        for(ModelLevel modelLevel : ModelLevel.values()){
            if(level == modelLevel.getLevel()){
                return modelLevel;
            }
        }
        return null;
    }
}
