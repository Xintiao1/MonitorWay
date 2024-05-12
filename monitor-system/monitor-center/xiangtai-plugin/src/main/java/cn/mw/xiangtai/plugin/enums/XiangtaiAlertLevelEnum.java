package cn.mw.xiangtai.plugin.enums;

/**
 * @author gengjb
 * @description 祥泰告警等级枚举
 * @date 2023/10/23 11:00
 */
public enum XiangtaiAlertLevelEnum {

    HIGHRISK(1, "高危"),

    MODERATERISK(2, "中危"),

    LOWRISK(3, "低危"),
    ;


    private int alertLevel;

    private String alertLevelName;

    XiangtaiAlertLevelEnum(int alertLevel, String alertLevelName) {
        this.alertLevel = alertLevel;
        this.alertLevelName = alertLevelName;
    }

    public int getAlertLevel() {
        return alertLevel;
    }

    public String getAlertLevelName() {
        return alertLevelName;
    }

    public static XiangtaiAlertLevelEnum getByAlertLevel(int level) {
        for (XiangtaiAlertLevelEnum alertLevelEnum : values()) {
            if (level == alertLevelEnum.getAlertLevel()) {
                return alertLevelEnum;
            }
        }
        return null;
    }
}
