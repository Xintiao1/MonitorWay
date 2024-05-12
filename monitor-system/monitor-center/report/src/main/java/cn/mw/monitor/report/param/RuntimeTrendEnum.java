package cn.mw.monitor.report.param;

public enum RuntimeTrendEnum {
    ALERT_TREND(0,"告警"),
    ASSET_TREND(1,"资产"),
    ASSET_UNNORMAL_TREND(2,"异常资产");
    private int code;
    private String name;

    RuntimeTrendEnum(int code, String name) {
        this.code=code;
        this.name=name;
    }
    public  Integer getCode(){
        return code;
    }
    public String getName(){
        return name;
    }

    public static RuntimeTrendEnum getByValue(int value){
        for (RuntimeTrendEnum code:RuntimeTrendEnum.values()) {
            if(code.getCode()==value){
                return code;
            }
        }
        return null;
    }
}
