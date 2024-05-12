package cn.mw.monitor.netflow.enums;

import cn.mw.monitor.netflow.entity.IP;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className FieldType
 * @description 字段类型属性
 * @date 2023/3/15
 */
public enum FieldType {
    /**
     * 字符串
     */
    TEXT(Arrays.asList(String.class, IP.class), "字符串类型"),

    /**
     * 数字类型
     */
    NUMBER(Arrays.asList(Integer.class, Long.class, Short.class, Date.class, int.class, long.class), "所有数字类型和时间类型");

    /**
     * 受影响的类
     */
    private List<Class> list;

    /**
     * 描述
     */
    private String desc;

    FieldType(List<Class> list, String desc) {
        this.list = list;
        this.desc = desc;
    }

    private List<Class> getList() {
        return list;
    }

    private String getDesc() {
        return desc;
    }

    public static FieldType getField(Class clazz) {
        for (FieldType fieldType : FieldType.values()) {
            if (fieldType.getList().contains(clazz)) {
                return fieldType;
            }
        }
        return null;
    }
}
