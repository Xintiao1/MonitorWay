package cn.mw.monitor.configmanage.common;

/**
 * @author gui.quanwang
 * @className DetectType
 * @description 合规检测对应的类别
 * @date 2021/12/17
 */
public enum DetectType {

    /**
     * 规则类别
     */
    RULE("RULE"),

    /**
     * 策略类别
     */
    POLICY("POLICY"),

    /**
     * 报告类别
     */
    REPORT("REPORT");


    private String detectName;

    DetectType(String detectName) {
        this.detectName = detectName;
    }

    public String getDetectName() {
        return detectName;
    }

    /**
     * 根据名称获取类别
     *
     * @param detectName 类型名称
     * @return
     */
    public static DetectType getTypeByName(String detectName) {
        for (DetectType type : values()) {
            if (type.getDetectName().equals(detectName)) {
                return type;
            }
        }
        return null;
    }
}
