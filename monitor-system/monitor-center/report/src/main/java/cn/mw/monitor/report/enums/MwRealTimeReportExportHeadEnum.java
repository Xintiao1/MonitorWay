package cn.mw.monitor.report.enums;

/**
 * @author gengjb
 * @description 实时报表导出表头枚举
 * @date 2023/11/2 10:30
 */
public enum MwRealTimeReportExportHeadEnum {
    ASSETS_NAME("assetsName", "资产名称"),
    ASSETS_IP("assetIp", "资产IP"),
    BUSINESS_SYSTEM("businessSystem", "业务系统"),
    ;


    private String field;

    private String fieldChnName;

    MwRealTimeReportExportHeadEnum(String field, String fieldChnName) {
        this.field = field;
        this.fieldChnName = fieldChnName;
    }

    public String getField() {
        return field;
    }

    public String getFieldChnName() {
        return fieldChnName;
    }

    public static String getFieldByName(String fieldChnName) {
        for (MwRealTimeReportExportHeadEnum reportExportHeadEnum : values()) {
            if (fieldChnName.equals(reportExportHeadEnum.getFieldChnName())) {
                return reportExportHeadEnum.getField();
            }
        }
        return null;
    }
}
