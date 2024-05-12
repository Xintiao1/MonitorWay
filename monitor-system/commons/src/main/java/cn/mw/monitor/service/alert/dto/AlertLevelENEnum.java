package cn.mw.monitor.service.alert.dto;

public enum AlertLevelENEnum {
    INFO("Information","信息"),
    WARNING("Warning","警告"),
    NORMAL("Average","一般严重"),
    ERROR("High","严重"),
    DISASTER("Disaster","紧急");


    private String nameEN;
    private String name;
    AlertLevelENEnum(String nameEN, String name){
        this.nameEN = nameEN;
        this.name = name;
    }
    public String getNameEN() {
        return nameEN;
    }
    public String getName() {
        return name;
    }
    public String getName(String nameEN) {
        for (AlertLevelENEnum levelEnum : AlertLevelENEnum.values()){
            if(levelEnum.getNameEN().equals(nameEN)){
                return levelEnum.getName();
            }
        }
        return null;
    }

}
