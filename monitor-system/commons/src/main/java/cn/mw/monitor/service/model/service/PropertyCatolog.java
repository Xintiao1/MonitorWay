package cn.mw.monitor.service.model.service;

public enum PropertyCatolog {
    Default(0, "默认属性")
    ,Basic(1,"基础信息")
    ,Monitor(2 ,"纳管属性")
    ,Advanced(3 ,"高级设置")
    ;

    private int code;
    private String desc;

    PropertyCatolog(int code ,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
