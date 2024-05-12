package cn.mw.monitor.model.param;

import cn.mw.monitor.service.assets.utils.RuleType;

/**
 * 数据库驱动类型
 */
public enum DataBaseDriverTypeEnum {
    MYSQL("mysql"),
    ORACLE("Oracle");
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    DataBaseDriverTypeEnum(String name) {
        this.name = name;
    }

    public static DataBaseDriverTypeEnum getInfoByName(String name) {
        for(DataBaseDriverTypeEnum r : DataBaseDriverTypeEnum.values()) {
            if(r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }
}
