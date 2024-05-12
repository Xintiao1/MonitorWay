package cn.mw.monitor.state;

public enum RuntimeReportState {
    CPU_MEMORY_LOSS(0,"cpu,内存,丢包率"),
    INTERFACE_DISK(1,"接口，磁盘"),
    ASSETUTILIZATION(2,"接口，磁盘");
    private int code;
    private String name;

    RuntimeReportState(int code, String name) {
        this.code=code;
        this.name=name;
    }
    public  Integer getCode(){
        return code;
    }
    public String getName(){
        return name;
    }

    public static RuntimeReportState getByValue(int value){
        for (RuntimeReportState code:RuntimeReportState.values()) {
            if(code.getCode()==value){
                return code;
            }
        }
        return null;
    }

}
