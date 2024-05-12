package cn.mw.monitor.report.service.impl;

public enum MwReportTypeEnum {
    LINK_REPORT("13"),
    LINK_REPORT_LYL("24"),
    DISK_REPORT("10"),
    ASSETSUSE_REPORT("12"),
    MPLS_REPORT("21"),
    IP_REPORT("22"),
    RUNSTATE_REPORT("7"),
    CPUMEMORY_REPORT("9"),
    CPUMEMORY_REPORT_LYL("23"),
    PATROL_INSPECTION("26"),
    CPU_REALTIME_REPORT("25"),
    UNKNOWN("");


    private String name;

    MwReportTypeEnum(String name){this.name = name;}

    public String getName() {
        return name;
    }

    public static MwReportTypeEnum getReportType(String name){
        for (MwReportTypeEnum typeEnum : MwReportTypeEnum.values()){
            if(typeEnum.getName().equals(name)){
                return typeEnum;
            }
        }
        MwReportTypeEnum typeEnum = MwReportTypeEnum.getReportType("");
        return typeEnum;
    }
}
