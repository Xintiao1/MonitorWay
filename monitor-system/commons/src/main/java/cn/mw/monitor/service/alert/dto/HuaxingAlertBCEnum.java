package cn.mw.monitor.service.alert.dto;

public enum HuaxingAlertBCEnum {
    TC("TC","T3"),
    CC("CC","T3"),
    FC("FC","T3"),
    MD("MD","T3"),
    TD("TD","T4"),
    ED("ED","T4"),
    HD("HD","T4"),
    MF("MF","T4"),
    TE("TE","T5"),
    CE("CE","T5"),
    FE("FE","T5"),
    ME("ME","TT54");


    private String code;
    private String name;
    HuaxingAlertBCEnum(String code, String name){
        this.code = code;
        this.name = name;
    }
    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public static String getName(String code) {
        for (HuaxingAlertBCEnum levelEnum : HuaxingAlertBCEnum.values()){
            if(levelEnum.getCode().equals(code)){
                return levelEnum.getName();
            }
        }
        return null;
    }

}
