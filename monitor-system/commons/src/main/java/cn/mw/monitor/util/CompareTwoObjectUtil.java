package cn.mw.monitor.util;

import cn.mw.monitor.service.server.api.CoreValueInterface;
import io.swagger.annotations.ApiModelProperty;

import java.lang.reflect.*;
import java.util.*;


public class CompareTwoObjectUtil {

    static List<Map<String, Object>> list = new ArrayList<>();

    public static List<Map<String, Object>> compareTwoClass(Object class1, Object class2) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        Class<?> clazz1 = class1.getClass();
        Class<?> clazz2 = class2.getClass();
        Field[] field1 = getAllFieldss(clazz1);
        Field[] field2 = getAllFieldss(clazz2);

        for (int i = 0; i < field1.length; i++) {
            if (field1[i].getName().equals(field2[i].getName())) {
                field1[i].setAccessible(true);
                field2[i].setAccessible(true);
                if (!compareTwo(field1[i].get(class1), field2[i].get(class2))) {
                    //基本类型，日期类型
                    if (field1[i].getType().toString().contains("java.lang") || field1[i].getType().toString().contains("java.util.Date")) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("new", field2[i].get(class2));
                        map.put("old", field1[i].get(class1));
                        map.put("name", field1[i].getName());
                        map = engToChinese(map, field1);
                        list.add(map);
                        continue;
                    }
                    //自定义类
                    if (!field1[i].getType().toString().contains("java.util") && !field1[i].getType().toString().contains("java.lang")) {
                        Object o1 = field1[i].get(class1);
                        Object o2 = field2[i].get(class2);
                        compareTwoClass(o1, o2);
                        continue;
                    }
                    //属性是List实现类
                    if (List.class.isAssignableFrom(field1[i].getType())) {
                        Type genericType = field1[i].getGenericType();
                        if (genericType instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) genericType;
                            Type actualTypeArgument = pt.getActualTypeArguments()[0];
                            //集合泛型是基本类型包装类型或集合类型 直接比较
                            if (actualTypeArgument.getTypeName().contains("java.lang") || actualTypeArgument.getTypeName().contains("java.util")) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("new", field2[i].get(class2));
                                map.put("old", field1[i].get(class1));
                                map.put("name", field1[i].getName());
                                engToChinese(map, field1);
                                list.add(map);
                                continue;
                            } else {
                                //集合泛型是自定义类型
                                //实现了coreValueInterface 的类获得核心的值，比如标签实体类中的标签名称，机构实体类中机构名称
                                if (CoreValueInterface.class.isAssignableFrom((Class) actualTypeArgument)) {
                                    Map<String, Object> map = new HashMap<>();
                                    Object o2 = field2[i].get(class2);
                                    List<CoreValueInterface> list2 = (List) o2;
                                    StringBuffer snew = new StringBuffer("");
                                    for (CoreValueInterface co : list2) {
                                        Class<?> aClass = co.getClass();
                                        Method getCoreValue = aClass.getMethod("getCoreValue");
                                        Object coreValue = getCoreValue.invoke(co);
                                        snew.append(coreValue + ";");
                                    }
                                    map.put("new", snew);
                                    Object o1 = field1[i].get(class1);
                                    List<CoreValueInterface> list1 = (List) o1;
                                    StringBuffer sold = new StringBuffer("");
                                    for (CoreValueInterface co : list1) {
                                        Class<?> aClass = co.getClass();
                                        Method getCoreValue = aClass.getMethod("getCoreValue");
                                        Object coreValue = getCoreValue.invoke(co);
                                        sold.append(coreValue + ";");
                                    }
                                    map.put("old", sold);
                                    map.put("name", field1[i].getName());
                                    engToChinese(map, field1);
                                    list.add(map);
                                } else {
                                    //没有实现指定接口的类且类属性有不一样，使用默认toSting打印对象
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("new", field2[i].get(class2));
                                    map.put("old", field1[i].get(class1));
                                    map.put("name", field1[i].getName());
                                    map = engToChinese(map, field1);
                                    list.add(map);
                                }
                            }
                        }
                        continue;
                    }
                    //其他情况
                    Map<String, Object> map = new HashMap<>();
                    map.put("new", field2[i].get(class2));
                    map.put("old", field1[i].get(class1));
                    map.put("name", field1[i].getName());
                    map = engToChinese(map, field1);
                    list.add(map);
                }
            }

        }
        return list;
    }

    /**
     * 获取当前类属性及父类属性
     *
     * @param clazz
     * @return
     */
    private static Field[] getAllFieldss(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        return fieldList.toArray(fields);
    }


    private static boolean compareTwo(Object object1, Object object2) {
        if (object1 == null && object2 == null) {
            return true;
        }
        if (object1 == null && object2 != null) {
            return false;
        }
        if (object1.equals(object2)) {
            return true;
        }
        return false;
    }

    public static List<Map<String, Object>> enToCn(Class<?> clazz, List<Map<String, Object>> list) throws ClassNotFoundException {
        //获取实体类属性
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            //属性是否被注解修饰，是则获取注解value即属性中文名
            if (field.isAnnotationPresent(ApiModelProperty.class)) {
                ApiModelProperty annotation = field.getAnnotation(ApiModelProperty.class);
                final String value = annotation.value();
                for (Map<String, Object> map : list) {
                    if (map.get("name").equals(field.getName())) {
                        map.put("name", value);
                    }
                }
            }
        }
        return list;
    }

    public static Map<String, Object> engToChinese(Map<String, Object> map, Field[] fields) {
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ApiModelProperty.class)) {
                ApiModelProperty annotation = field.getAnnotation(ApiModelProperty.class);
                final String value = annotation.value();
                if (map.get("name").equals(field.getName())) {
                    map.put("name", value);
                }
            }
        }
        return map;
    }

    public static String getFormateString(List<Map<String, Object>> list) {
        StringBuffer sb = new StringBuffer("");
        for (Map<String, Object> map : list) {
            sb.append("[" + map.get("name") + "]由<").append(map.get("old").toString() + ">改为<").
                    append(map.get("new").toString() + ">;");
        }
        return sb.toString();
    }
}
