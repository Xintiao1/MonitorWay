package cn.mw.monitor.service.systemLog.dto;

public enum LogTypeEnum {
    LOGINLOG(1,"登录日志"),
    SYSLOG(0,"系统日志"),
    OPERATELOG(2,"执行日志"),
    SYSTABLENAME(0,"mw_system_log"),
    LOGINTABLENAME(1,"mw_login_log");

    private int code;
    private String name;

    LogTypeEnum(int code, String name) {
        this.code=code;
        this.name=name;
    }

    public  Integer getCode(){
        return code;
    }
    public String getName(){
        return name;
    }

    public static LogTypeEnum getByValue(int value){
        for (LogTypeEnum code : LogTypeEnum.values()) {
            if(code.getCode()==value){
                return code;
            }
        }
        return null;
    }
}
