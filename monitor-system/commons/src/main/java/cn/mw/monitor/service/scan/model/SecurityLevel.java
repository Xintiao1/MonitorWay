package cn.mw.monitor.service.scan.model;

import cn.mw.monitor.service.virtual.dto.VirtualizationType;

public enum SecurityLevel {

    authPriv(2,"authPriv"), noAuthNoPriv(0,"noAuthNoPriv")
    ,authNoPriv(1,"authNoPriv");

    private int code;
    private String name;

    SecurityLevel(int code, String name){
        this.code = code;
        this.name = name;
    }

    public int getCode(){
        return this.code;
    }

    public String getName() {
        return name;
    }

    public static String getName(Integer code){
        for(SecurityLevel securityLevelEnum : SecurityLevel.values()){
            if(code.equals(securityLevelEnum.getCode())){
                return securityLevelEnum.getName();
            }
        }
        return null;
    }
}
