package cn.mw.monitor.visualized.enums;

/**
 * @author gengjb
 * @description 可视化评分类型枚举
 * @date 2023/9/27 10:26
 */
public enum VisualizedScoreTypeEnum {

    /**
     * 业务分类
     */
    BUSINESS_CLASSIFY(1, "BUSINESS_CLASSIFY"),
    /**
     * 资产类型
     */
    ASSETS_TYPE(2, "ASSETS_TYPE"),
    /**
     * 资产名称
     */
    ASSETS_NAME(3, "ASSETS_NAME");

    private int type;

    private String typeNmae;

    VisualizedScoreTypeEnum(int type, String typeNmae) {
        this.type = type;
        this.typeNmae = typeNmae;
    }

    public int getType() {
        return type;
    }

    public static VisualizedScoreTypeEnum getByType(int type) {
        for (VisualizedScoreTypeEnum scoreTypeEnum : values()) {
            if (type == scoreTypeEnum.getType()) {
                return scoreTypeEnum;
            }
        }
        return null;
    }
}
