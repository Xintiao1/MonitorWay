package cn.mw.monitor.es.action;

import cn.mw.monitor.annotation.ESString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class EsUtil {
    public static final String MW_PACKAGE_PREFIX = "cn.mw.monitor";
    private static final String dataFormatStr = "yyyyMMddHHmmss";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(dataFormatStr);

    public static Map<String, Method> getMethodMap(Class clazz) throws Exception {
        Map<String, Method> methodMap = new HashMap<>();
        List<Field> fields = getAllFields(clazz);
        for(Field field : fields){
            String methodName = getMethodName(field.getName());
            Method method = clazz.getMethod(methodName);
            methodMap.put(field.getName() ,method);
        }
        return methodMap;
    }

    private static String getMethodName(String fildeName) {
        byte[] items = fildeName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return "get" + new String(items);
    }

    public static Map<String, Object> getColumnMap(Class clazz){
        Map<String, Object> columnMap = new HashMap<>();
        List<Field> fields = getAllFields(clazz);
        for(Field field : fields){
            Map fieldDefinition = null;
            Class fieldClazz = field.getType();
            if(fieldClazz == Integer.class || fieldClazz == int.class){
                fieldDefinition = doESMappingByInteger();

            }else if (fieldClazz == String.class){
                boolean hasKeyword = true;
                Annotation annotation = field.getAnnotation(ESString.class);
                if(null != annotation){
                    ESString esString = (ESString) annotation;
                    if(!esString.hasKeyword()){
                        hasKeyword = false;
                    }
                }
                fieldDefinition = doESMappingByString(hasKeyword);

            }else if (fieldClazz == Date.class){
                fieldDefinition = doESMappingByDate();

            }else if (fieldClazz == Boolean.class || fieldClazz == boolean.class){
                fieldDefinition = doESMappingByBoolean();

            }else if(fieldClazz.isArray() && fieldClazz.getComponentType() == byte.class){
                fieldDefinition = doESMappingByBinary();

            }else if(fieldClazz.getCanonicalName().indexOf(MW_PACKAGE_PREFIX) >= 0){
                fieldDefinition = doESMappingByStruct();
            }
            if(null != fieldDefinition){
                columnMap.put(field.getName() ,fieldDefinition);
            }
        }
        return columnMap;
    }

    private static Map doESMappingByInteger(){
        Map value = new HashMap();
        value.put("type", "long");
        return value;
    }

    private static Map doESMappingByString(){
        return doESMappingByString(true);
    }

    private static Map doESMappingByString(boolean hasKeyword){
        Map value = new HashMap();
        Map fields = new HashMap();
        Map type = new HashMap();
        value.put("type", "text");

        if(hasKeyword){
            value.put("fields", fields);
            fields.put("keyword", type);
            type.put("type", "keyword");
        }

        return value;
    }

    private static Map doESMappingByDate(){
        Map value = new HashMap();
        value.put("type", "date");
        value.put("format", dataFormatStr + "||yyyy-MM-dd||epoch_millis");
        return value;
    }

    private static Map doESMappingByBoolean(){
        Map value = new HashMap();
        value.put("type", "boolean");
        return value;
    }


    private static Map doESMappingByStruct(){
        Map value = new HashMap();
        value.put("type", "nested");
        return value;
    }

    private static Map doESMappingByBinary(){
        Map value = new HashMap();
        value.put("type", "binary");
        value.put("index", false);
        value.put("doc_values", false);
        return value;
    }

    private static Map doESMappingByIP(){
        Map value = new HashMap();
        value.put("type", "ip");
        return value;
    }

    private static List<Field> getAllFields(Class clazz){
        List<Field> fields = new ArrayList<>();
        getFieldListWithParent(clazz ,fields);
        return fields;
    }

    private static void getFieldListWithParent(Class clazz , List<Field> list){
        list.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class parent = clazz.getSuperclass();
        if(parent.getCanonicalName().indexOf(MW_PACKAGE_PREFIX) >=0){
            getFieldListWithParent(parent ,list);
        }
    }

    public static Map transformData(Object data , Map<String, Method> getMethodMap) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        Class clazz = data.getClass();
        List<Field> fields = getAllFields(clazz);
        for(Field field : fields){
            Class fieldClazz = field.getType();
            Method method = getMethodMap.get(field.getName());
            if(null != method){
                Object value = null;
                if(fieldClazz == Date.class){
                    Date dateVal = (Date) method.invoke(data);
                    value = dateFormat.format(dateVal);
                }else{
                    value = method.invoke(data);
                }
                dataMap.put(field.getName() ,value);
            }
        }
        return dataMap;
    }
}
