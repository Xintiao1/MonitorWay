package cn.mw.monitor.service.alert.dto;

public enum AlertLevelEnum {
    OTHER("0","未分类"),
    INFO("1","信息"),
    WARNING("2","警告"),
    NORMAL("3","一般严重"),
    ERROR("4","严重"),
    DISASTER("5","紧急");


    private String code;
    private String name;
    AlertLevelEnum(String code, String name){
        this.code = code;
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getName(String code) {
        for (AlertLevelEnum levelEnum : AlertLevelEnum.values()){
            if(levelEnum.getCode().equals(code)){
                return levelEnum.getName();
            }
        }
        return null;
    }

}
