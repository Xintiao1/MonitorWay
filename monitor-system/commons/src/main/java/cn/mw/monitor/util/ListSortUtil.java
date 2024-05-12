package cn.mw.monitor.util;

import cn.mw.monitor.service.virtual.dto.BasicTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.UNKNOWN;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author syt
 * @Date 2021/4/20 17:13
 * @Version 1.0
 */
@Slf4j
public class ListSortUtil<T> {
    /**
     * @param list      需要排序的集合
     * @param sortField 要排序的集合中的实体类的某个字段
     * @param sortMode  排序的方式（升序0/降序1）
     */
    public List<T> sort(List<T> list, final String sortField, final int sortMode) {
        if (list == null || list.size() < 2) {
            return list;
        }
        //首字母转大写
        String newStr = sortField.substring(0, 1).toUpperCase() + sortField.substring(1);

        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                try {
                    Class aClass = o1.getClass();
                    Field field = null;
                    try {
                        field = aClass.getDeclaredField(sortField);//获取成员变量
                    }catch (NoSuchFieldException e) {
                        field = aClass.getSuperclass().getDeclaredField(sortField);//获取父类成员变量
                    }
                    field.setAccessible(true);//设置成可访问状态
                    String typeName = field.getType().getName().toUpperCase();//转换成大写
                    Object v1 = field.get(o1);
                    Object v2 = field.get(o2);
                    boolean ASC_order = (sortMode == 0);
                    typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
                    BasicTypeEnum value = BasicTypeEnum.valueOf(typeName);
                    if (null == v1) {
                        return 1;
                    }
                    if (null == v2) {
                        return -1;
                    }
                    switch (value) {
                        case INT:
                        case INTEGER:
                            Integer integer1 = Integer.parseInt(v1.toString());
                            Integer integer2 = Integer.parseInt(v2.toString());
                            return ASC_order ? integer1.compareTo(integer2) : integer2.compareTo(integer1);
                        case BYTE:
                            Byte byte1 = Byte.parseByte(v1.toString());
                            Byte byte2 = Byte.parseByte(v2.toString());
                            return ASC_order ? byte1.compareTo(byte2) : byte2.compareTo(byte1);
                        case CHAR:
                            Integer char1 = (int) (v1.toString().charAt(0));
                            Integer char2 = (int) (v2.toString().charAt(0));
                            return ASC_order ? char1.compareTo(char2) : char2.compareTo(char1);
                        case DATE:
                            Date date1 = (Date) (v1);
                            Date date2 = (Date) (v2);
                            return ASC_order ? date1.compareTo(date2) : date2.compareTo(date1);
                        case LONG:
                            Long long1 = Long.parseLong(v1.toString());
                            Long long2 = Long.parseLong(v2.toString());
                            return ASC_order ? long1.compareTo(long2) : long2.compareTo(long1);
                        case FLOAT:
                            Float float1 = Float.parseFloat(v1.toString());
                            Float float2 = Float.parseFloat(v2.toString());
                            return ASC_order ? float1.compareTo(float2) : float2.compareTo(float1);
                        case SHORT:
                            Short short1 = Short.parseShort(v1.toString());
                            Short short2 = Short.parseShort(v2.toString());
                            return ASC_order ? short1.compareTo(short2) : short2.compareTo(short1);
                        case DOUBLE:
                            Double double1 = Double.parseDouble(v1.toString());
                            Double double2 = Double.parseDouble(v2.toString());
                            return ASC_order ? double1.compareTo(double2) : double2.compareTo(double1);
                        case STRING:
                            String string1 = v1.toString();
                            String string2 = v2.toString();
                            return ASC_order ? string1.compareTo(string2) : string2.compareTo(string1);
                        case BOOLEAN:
                            Boolean boolean1 = Boolean.parseBoolean(v1.toString());
                            Boolean boolean2 = Boolean.parseBoolean(v2.toString());
                            return ASC_order ? boolean1.compareTo(boolean2) : boolean2.compareTo(boolean1);
                        case TIMESTAMP:
                            Timestamp timestamp1 = (Timestamp) (v1);
                            Timestamp timestamp2 = (Timestamp) (v2);
                            return ASC_order ? timestamp1.compareTo(timestamp2) : timestamp2.compareTo(timestamp1);
                        default:
                            //调用对象的compareTo()方法比较大小
                            Method method = field.getType().getDeclaredMethod("compareTo", new Class[]{field.getType()});
                            method.setAccessible(true);
                            int result = (Integer) method.invoke(v1, new Object[]{v2});
                            return ASC_order ? result : result * (-1);
                    }
                } catch (Exception e) {
                    log.error("fail to  methodStr:{} cause:{}", newStr, e);
                }
                return 0;
            }
        });
        return list;
    }
}
