package cn.mw.monitor.report.enums;

/**
 * @author gengjb
 * @description 磁盘信息枚举
 * @date 2024/1/29 10:03
 */
public enum MwReportDiskEnum {
    MW_DISK_TOTAL("MW_DISK_TOTAL","diskTotal","磁盘总量"),
    MW_DISK_USED("MW_DISK_USED","diskUse","磁盘已使用"),
    MW_DISK_UTILIZATION("MW_DISK_UTILIZATION","diskUtilization","磁盘利用率")
    ;


    MwReportDiskEnum(String itemName, String field, String desc) {
        this.itemName = itemName;
        this.field = field;
        this.desc = desc;
    }

    private String itemName;

    private String field;

    private String desc;

    public String getItemName() {
        return itemName;
    }

    public String getField() {
        return field;
    }

    public String getDesc() {
        return desc;
    }

    public static MwReportDiskEnum getDiskEnum(String itemItem) {
        for (MwReportDiskEnum diskEnum : values()) {
            if (itemItem.equals(diskEnum.getItemName())) {
                return diskEnum;
            }
        }
        return null;
    }
}
