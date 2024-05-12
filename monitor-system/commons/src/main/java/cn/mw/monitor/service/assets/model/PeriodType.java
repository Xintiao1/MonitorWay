package cn.mw.monitor.service.assets.model;

public enum PeriodType {
    once(1 ,"一次性"), day(2 ,"每日")
    ,week(3, "每周") ,month(4,"每月");
    private int code;
    private String chnName;

    PeriodType(int code ,String chnName){
        this.code = code;
        this.chnName = chnName;
    }

    public int getCode() {
        return code;
    }

    public String getChnName() {
        return chnName;
    }
}
