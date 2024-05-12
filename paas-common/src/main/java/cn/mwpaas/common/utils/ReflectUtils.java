package cn.mwpaas.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author phzhou
 *
 * 反射工具类
 * @ClassName ReflectUtils
 * @CreateDate 2019/4/1
 * @Description
 */
public class ReflectUtils {

    /**
     * 获取传入对象的字段值，含父类
     */
    public static Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        if (obj == null) {
            return null;
        }
        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("fieldName is null");
        }
        Class clazz = obj.getClass();
        Field field = null;
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
            if (field != null) {
                break;
            }
        }

        if (field == null) {
            throw new NoSuchFieldException();
        }

        field.setAccessible(true);  //设为true可以访问受限制的变量,默认为false ，这样将会破坏访问规则，但是某些时候却必须要访问(序列化、持久化)
        return field.get(obj);
    }

    /**
     * 设置某个属性值
     * @param obj
     * @param fieldName
     * @param value
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static boolean setFieldValue(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        if (obj == null) {
            return false;
        }
        if (StringUtils.isBlank(fieldName)) {
            return false;
        }
        Field field = null;
        Class clazz = obj.getClass();
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
            if (field != null) {
                break;
            }
        }

        if (field == null) {
            throw new NoSuchFieldException();
        }

        field.setAccessible(true);
        field.set(obj, value);
        return true;
    }

    /**
     * 调用某个对象的方法，包括静态方法
     */
    public static Object invoke(Object obj, String methodName, Object... paramValues) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (obj == null) {
            throw new NullPointerException();
        }
        if (StringUtils.isBlank(methodName)) {
            return new NullPointerException();
        }

        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] paramClazz = method.getParameterTypes();
                if (paramClazz != null && paramClazz.length == paramValues.length) {
                    for (int i = 0; i < paramClazz.length; i++) {
                        if (!paramClazz[i].equals(paramValues[i].getClass())) {
                            break;
                        }
                    }
                    method.setAccessible(true);
                    return method.invoke(obj, paramValues);
                }
            }
        }
        throw new NoSuchMethodException();
    }

    /**
     * 获取属性的get方法
     *
     * @param field
     * @return
     */
    public static String getGetMethodName(Field field) {
        char[] nameArr = field.getName().toCharArray();
        char first = nameArr[0];
        if (first >= 97 && first <= 122) {
            first ^= 32;
        }
        nameArr[0] = first;
        return "get" + String.valueOf(nameArr);
    }
}
