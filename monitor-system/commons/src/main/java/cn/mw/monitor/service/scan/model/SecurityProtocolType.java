package cn.mw.monitor.service.scan.model;

public enum SecurityProtocolType {

    MD5(0,"MD5", 1,0)
    , SHA(1,"SHA",2,1)
    , DES(2,"DES",2,0)
    , AES(3,"AES",1,1);

    private int code;
    private String name;
    private int dropDownMapCode;
    private int interfaceCode;

    SecurityProtocolType(int code, String name, int dropDownMapCode, int interfaceCode){
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
}


