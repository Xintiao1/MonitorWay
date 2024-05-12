package cn.mw.monitor.model.param;

public enum ModelTriggerLevelType {
    LEVEL_ONE("5","一级"),
    LEVEL_TWO("4","二级"),
    LEVEL_THREE("2","三级"),
    LEVEL_OTHER("-1","未定义");

   private String level;
   private String name;

    ModelTriggerLevelType(String level, String name) {
        this.level = level;
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static String getName(String type){
        for(ModelTriggerLevelType checkModelEnum : ModelTriggerLevelType.values()){
            if(type.equals(checkModelEnum.getName())){
                return checkModelEnum.getName();
            }
        }
        return null;
    }
}
