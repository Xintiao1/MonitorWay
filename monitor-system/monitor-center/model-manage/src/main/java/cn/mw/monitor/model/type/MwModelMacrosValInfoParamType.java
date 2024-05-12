package cn.mw.monitor.model.type;

public enum MwModelMacrosValInfoParamType {
    Input("input" ,"0","输入框")
    ,EncInput("encinput" ,"1","加密输入")
    ,EngineSel("syncEngine" ,"2","同步引擎");

    private String name;
    private String desc;
    private String code;

    MwModelMacrosValInfoParamType(String name ,String code ,String desc){
        this.name = name;
        this.desc = desc;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
}
