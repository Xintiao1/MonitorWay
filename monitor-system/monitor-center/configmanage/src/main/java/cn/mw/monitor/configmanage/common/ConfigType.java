package cn.mw.monitor.configmanage.common;

/**
 * @author gui.quanwang
 * @className ConfigType
 * @description 类别数据枚举
 * @date 2022/1/4
 */
public enum ConfigType {
    /**
     * 全部数据
     */
    ALL(0, "all", "所有配置类别"),

    /**
     * running
     */
    RUNNING(1, "Running", "对应mw_ncm_downloadconfig_table表的数据"),

    /**
     * start-up
     */
    STARTUP(2, "Startup", "对应mw_ncm_downloadconfig_table表的数据");

    /**
     * 列别对应的数字
     */
    private int typeNumber;

    /**
     * 对应数据库的名称
     */
    private String dataBaseValue;

    /**
     * 描述
     */
    private String desc;

    public int getTypeNumber() {
        return typeNumber;
    }

    public String getDataBaseValue() {
        return dataBaseValue;
    }

    ConfigType(int typeNumber, String dataBaseValue, String desc) {
        this.typeNumber = typeNumber;
        this.dataBaseValue = dataBaseValue;
        this.desc = desc;
    }

    /**
     * 根据类别获取对应的枚举
     *
     * @param number 类别
     * @return
     */
    public static ConfigType getByNumber(int number) {
        for (ConfigType type : values()) {
            if (type.getTypeNumber() == number) {
                return type;
            }
        }
        return ALL;
    }

    @Override
    public String toString() {
        return "ConfigType{" +
                "typeNumber=" + typeNumber +
                ", dataBaseValue='" + dataBaseValue + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
