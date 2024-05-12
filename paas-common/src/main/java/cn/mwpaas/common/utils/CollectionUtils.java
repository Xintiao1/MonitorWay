package cn.mwpaas.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author phzhou
 * @ClassName CollectionUtils
 * @CreateDate 2019/2/22
 * @Description
 */
@Slf4j
public class CollectionUtils {

    /**
     * 集合是否为空
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 集合是否为非空
     *
     * @param collection 集合
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return false == isEmpty(collection);
    }

    /**
     * list转map通用方法
     *
     * @param list          需要转换的list
     * @param keyMethodName 需要作键值的属性对应的方法
     * @param clazz         具体类
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> list2Map(List<V> list, String keyMethodName, Class<V> clazz) {
        Map<K, V> map = new HashMap();
        if (list != null) {
            try {
                Method methodGetKey = clazz.getMethod(keyMethodName);
                for (int i = 0; i < list.size(); i++) {
                    V value = list.get(i);
                    @SuppressWarnings("unchecked")
                    K key = (K) methodGetKey.invoke(list.get(i));
                    map.put(key, value);
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
        return map;
    }

    /**
     * list转map通用方法(map中的value是个list)
     *
     * @param list          需要转换的list
     * @param keyMethodName 需要作键值的属性对应的方法
     * @param clazz         具体类
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, List<V>> list2MapList(List<V> list, String keyMethodName, Class<V> clazz) {
        Map<K, List<V>> map = new HashMap();
        if (list != null) {
            try {
                Method methodGetKey = clazz.getMethod(keyMethodName);
                for (int i = 0; i < list.size(); i++) {
                    V value = list.get(i);
                    @SuppressWarnings("unchecked")
                    K key = (K) methodGetKey.invoke(list.get(i));
                    if (map.containsKey(key)) {
                        List<V> valueList = map.get(key);
                        valueList.add(value);
                        map.put(key, valueList);
                    } else {
                        List<V> valueList = new ArrayList<>();
                        valueList.add(value);
                        map.put(key, valueList);
                    }
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        }
        return map;
    }

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }
}
