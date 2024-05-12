package cn.mw.monitor.netflow.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gui.quanwang
 * @className OperateType
 * @description 流量明细——操作符号
 * @date 2023/3/15
 */
public enum OperateType {
    /**
     * 等于
     */
    EQUAL(":", FieldType.values()),

    /**
     * 等于任意值
     */
    EQUAL_ALL(":*", FieldType.values()),

    /**
     * 大于
     */
    GREATER_THAN(">", FieldType.NUMBER),

    /**
     * 大于等于
     */
    GREATER_THAN_EQUAL_TO(">=", FieldType.NUMBER),

    /**
     * 小于
     */
    LESS_THAN("<", FieldType.NUMBER),

    /**
     * 小于等于
     */
    LESS_THAN_EQUAL_TO("<=", FieldType.NUMBER),
    ;


    private String operateValue;

    private FieldType[] fieldTypes;

    OperateType(String operateValue, FieldType... fieldTypes) {
        this.operateValue = operateValue;
        this.fieldTypes = fieldTypes;
    }

    public String getOperateValue() {
        return operateValue;
    }

    private FieldType[] getFieldTypes() {
        return fieldTypes;
    }

    /**
     * 获取字段中包含的所有操作数据
     *
     * @param fieldType 字段内容
     * @return
     */
    public static List<OperateType> getOperateValueList(FieldType fieldType) {
        List<OperateType> list = new ArrayList<>();
        if (fieldType == null) {
            return list;
        }
        for (OperateType value : OperateType.values()) {
            for (FieldType type : value.getFieldTypes()) {
                if (type == fieldType) {
                    list.add(value);
                }
            }
        }
        return list;
    }

    /**
     *
     * @param operateValue
     * @return
     */
    public static OperateType getOperateType(String operateValue) {
        for (OperateType operateType : values()) {
            if (operateType.getOperateValue().equalsIgnoreCase(operateValue)) {
                return operateType;
            }
        }
        return null;
    }
}
