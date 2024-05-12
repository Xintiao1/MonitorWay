package cn.mw.monitor.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.BeanMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author syt
 * @Date 2021/1/12 10:02
 * @Version 1.0
 */
public class ListMapObjUtils {
    /**
     * 将List<T>转换成List<Map>
     *
     * @param objList
     * @param <T>
     * @return
     */
    public static <T> List<Map> objectsToMaps(List<T> objList) {
        List<Map> list = Lists.newArrayList();
        if (objList != null && objList.size() > 0) {
            Map map = null;
            T bean = null;
            for (int i = 0, size = objList.size(); i < size; i++) {
                bean = objList.get(i);
                map = beanToMap(bean);
                list.add(map);
            }
        }
        return list;
    }


    public static <T> List<Map<String, Object>> convertList(List<T> originalList) {
        List<Map<String, Object>> convertedList = new ArrayList<>();
        for (T entity : originalList) {
            Class<?> clazz = entity.getClass();
            Field[] fields = clazz.getDeclaredFields();
            Map<String, Object> map = new HashMap<>();
            for (Field field : fields) {
                field.setAccessible(true);
                String key = field.getName();
                try {
                    Object value = field.get(entity);
                    map.put(key, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            convertedList.add(map);
        }
        return convertedList;
    }

    /**
     * 将对象转换成map
     *
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> Map beanToMap(T bean) {
        Map map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }

    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) throws Exception {
        T bean = clazz.newInstance();
        BeanMap beanMap = BeanMap.create(bean);
        beanMap.putAll(map);
        return bean;
    }


    public static <T> T toObjByReflect(Map<String, Object> map, Class<T> type) throws Exception {
        T obj = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        obj = type.newInstance();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            f.setAccessible(true);
            if (map.containsKey(f.getName())) {
                if (f.getType() == Date.class) {
                    f.set(obj, sdf.parse((String) map.get(f.getName())));
                } else {
                    f.set(obj, map.get(f.getName()));
                }
            }
        }

        return obj;
    }

    /**
     * List<Map>转List<T>
     */
    public static <T> List<T> castMapToBean(List<Map<String, Object>> list, Class<T> clazz) throws Exception {
        if (list == null || list.size() == 0) {
            return null;
        }
        List<T> tList = new ArrayList<T>();
        // 获取类中声明的所有字段
        Field[] fields = clazz.getDeclaredFields();

        T t;
        for (Map<String, Object> map : list) {
            // 每次都先初始化一遍,然后再设置值
            t = clazz.newInstance();
            for (Field field : fields) {
                // 把序列化的字段去除掉
                if (!"serialVersionUID".equals(field.getName())) {
                    // 由于Field都是私有属性，所有需要允许修改
                    field.setAccessible(true);

                    // 设置值, 类型要和vo中的属性名称对应好,不然会报类型转换错误
                    field.set(t, convert(map.get(field.getName()), field.getType()));
                }
            }
            tList.add(t); // 把转换好的数据添加到集合中
        }
        return tList;
    }

    /**
     * Field类型转换
     */
    private static <T> T convert(Object obj, Class<T> type) {
        if (obj != null && StringUtils.isNotBlank(obj.toString())) {
            if (type.equals(String.class)) {
                return (T) obj.toString();
            } else if (type.equals(BigDecimal.class)) {
                return (T) new BigDecimal(obj.toString());
            } else if (type.equals(int.class)) {
                return (T) obj;
            } else if (type.equals(Integer.class)) {
                return (T) Integer.valueOf(obj.toString());
            } else {
                return (T) obj;
            }
            //其他类型转换......
        }
        return null;
    }

}
