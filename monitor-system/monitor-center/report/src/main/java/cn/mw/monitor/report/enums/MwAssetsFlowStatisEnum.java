package cn.mw.monitor.report.enums;

/**
 * @author gengjb
 * @description 资产流量统计枚举
 * @date 2023/8/28 16:49
 */
public enum MwAssetsFlowStatisEnum {

    NORMAL("NORMAL","正常"),
    ABNORMAL("ABNORMAL","异常"),
    UNKNOWN("UNKNOWN","异常"),
    SHUTDOWN("SHUTDOWN","未监控"),;

     private String name;

     private String desc;

    MwAssetsFlowStatisEnum( String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public static String getDesc(String name) {
        MwAssetsFlowStatisEnum[] values = values();
        for (MwAssetsFlowStatisEnum value : values) {
            if(name.contains(value.getName())){
                return value.desc;
            }
        }
        return null;
    }
}
