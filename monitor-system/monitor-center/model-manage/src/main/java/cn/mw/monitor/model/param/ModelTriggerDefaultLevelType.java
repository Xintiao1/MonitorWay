package cn.mw.monitor.model.param;

public enum ModelTriggerDefaultLevelType {
    LEVEL_ONE("5","紧急"),
    LEVEL_TWO("4","严重"),
    LEVEL_THREE("3","一般严重"),
    LEVEL_FOUR("2","告警"),
    LEVEL_FIVE("1","信息"),
    LEVEL_OTHER("-1","未分类");

   private String level;
   private String name;

    ModelTriggerDefaultLevelType(String level, String name) {
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
        for(ModelTriggerDefaultLevelType checkModelEnum : ModelTriggerDefaultLevelType.values()){
            if(type.equals(checkModelEnum.getName())){
                return checkModelEnum.getName();
            }
        }
        return null;
    }
}
