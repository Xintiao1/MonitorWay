package cn.mw.monitor.model.param;

import cn.mw.monitor.service.scan.model.SecurityLevel;

public enum MwModelPrivProtocolType {
    DES(0,"DES",2,0)
    , AES(1,"AES",1,1);

    private int code;
    private String name;
    private int dropDownMapCode;
    private int interfaceCode;

    MwModelPrivProtocolType(int code, String name, int dropDownMapCode, int interfaceCode){
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
        for(MwModelPrivProtocolType privProtocolEnum : MwModelPrivProtocolType.values()){
            if(code.equals(privProtocolEnum.getCode())){
                return privProtocolEnum.getName();
            }
        }
        return null;
    }
}


