package cn.mw.monitor.netflow.enums;

import cn.mw.monitor.netflow.entity.IP;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className IconType
 * @description 图标类型
 * @date 2023/4/12
 */
public enum IconType {
    /**
     * 字符串类型
     */
    TEXT("text", Arrays.asList(String.class, byte.class)),

    /**
     * 数字类型
     */
    NUMBER("number", Arrays.asList(Integer.class, Long.class, Short.class, int.class, long.class)),

    /**
     * 时间类型
     */
    DATE("date", Arrays.asList(Date.class)),

    /**
     * IP类型
     */
    IP("ip", Arrays.asList(IP.class));

    /**
     * 识别代码
     */
    private String columnCode;

    /**
     * 支持的类
     */
    private List<Class> classList;

    IconType(String columnCode, List<Class> classList) {
        this.columnCode = columnCode;
        this.classList = classList;
    }

    public String getColumnCode() {
        return columnCode;
    }

    private List<Class> getClassList() {
        return classList;
    }

    public static IconType getIconType(Class clazz) {
        for (IconType iconType : values()) {
            if (iconType.getClassList().contains(clazz)) {
                return iconType;
            }
        }
        return IconType.TEXT;
    }
}
