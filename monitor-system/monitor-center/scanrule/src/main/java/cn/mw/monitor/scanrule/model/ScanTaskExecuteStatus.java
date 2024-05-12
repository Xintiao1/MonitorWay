package cn.mw.monitor.scanrule.model;

/**
 * 扫描任务执行状态枚举
 */
public enum ScanTaskExecuteStatus {
    TOBE_EXECUTE(1,"待执行"),
    EXECUTE_IN(2,"执行中"),
    EXECUTE_COMPLETE(3,"执行完成");

    private Integer code;
    private String  name;

    ScanTaskExecuteStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getName(){
        return this.name;
    }
}
