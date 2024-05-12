package cn.mw.monitor.util;


public enum ModuleNameDesc {
    NET(2,"网络设备"),
    SAVE(3,"安全设备"),
    LINE(9,"线路"),
    SERVER(1,"服务器"),
    APP(6,"应用"),
    MIDDLEWARE(7,"中间件"),
    DATABASE(8,"数据库"),
    OUT_OF_BAND_ASSETS(69,"带外资产"),
    STORAGE(4,"存储设备"),
    WEB(11,"WEB");

    private  Integer code;
    private  String name;
    ModuleNameDesc(Integer code, String name){
        this.code = code;
        this.name = name;
    }
    public void setCode(Integer code) {
        this.code = code;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getCode() { return code; }
    public String getName() {
        return name;
    }


    public static String getModuleDescEnum(Integer code){
        for (ModuleNameDesc val : ModuleNameDesc.values()){
            if(val.getCode().equals(code)){
                return val.getName();
            }
        }
        return null;
    }
}
