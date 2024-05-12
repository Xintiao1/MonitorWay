package cn.mw.monitor.configmanage.common;

/**
 * @author gui.quanwang
 * @className DetectMatchLevel
 * @description 匹配等级枚举类
 * @date 2021/12/31
 */
public enum DetectMatchLevel {
    /**
     * 普通等级
     */
    NROMAL(0, "NROMAL"),
    /**
     * 警告等级
     */
    WARNING(1, "WARNING"),
    /**
     * 严重等级
     */
    ERROR(2, "ERROR"),
    /**
     * 匹配失败
     */
    FAILED(3, "FAILED");


    private int level;

    private String levelName;

    DetectMatchLevel(int level, String levelName) {
        this.level = level;
        this.levelName = levelName;
    }

    public int getLevel() {
        return level;
    }

    public static DetectMatchLevel getByLevel(int level) {
        for (DetectMatchLevel detectMatchLevel : values()) {
            if (level == detectMatchLevel.getLevel()) {
                return detectMatchLevel;
            }
        }
        return null;
    }
}
