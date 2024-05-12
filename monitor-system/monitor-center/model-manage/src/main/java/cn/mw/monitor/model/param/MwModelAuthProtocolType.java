package cn.mw.monitor.model.param;

public enum MwModelAuthProtocolType {

    MD5(0,"MD5", 1,0)
    , SHA(1,"SHA",2,1);

    private int code;
    private String name;
    private int dropDownMapCode;
    private int interfaceCode;

    MwModelAuthProtocolType(int code, String name, int dropDownMapCode, int interfaceCode){
        this.code = code;
        this.name = name;
        this.dropDownMapCode = dropDownMapCode;
        this.interfaceCode = interfaceCode;
    }

    public int getDropDownMapCode() {
        return dropDownMapCode;
    }

    public int getInterfaceCode(){
        return this.interfaceCode;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code){
        for(MwModelAuthProtocolType authProtocolEnum : MwModelAuthProtocolType.values()){
            if(code.equals(authProtocolEnum.getCode())){
                return authProtocolEnum.getName();
            }
        }
        return null;
    }
}


