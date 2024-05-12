package cn.mw.monitor.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONUtil {
    private static String SEP = "/";
    public static final String EMPTY = "";
    /**
     * 替换所有空格，留下一个
     */
    private static final String REPLACE_BLANK_ENTER = "\\s{2,}|\t|\r|\n";
    private static final Pattern REPLACE_P = Pattern.compile(REPLACE_BLANK_ENTER);

    public static <T> List<T> extractList(String paths , JSONObject jsonObject , Class<T> clazz
            , Map<String ,String> nameMap) throws Exception{

        List<T> ret = null;
        String[] pathArr = paths.split(SEP);
        JSONObject object = jsonObject.getJSONObject(pathArr[0]);
        if(pathArr.length > 1) {

            if(pathArr.length > 2) {
                for (int i = 1; i < pathArr.length - 1; i++) {
                    object = object.getJSONObject(pathArr[i]);
                    if (null == object) {
                        return null;
                    }
                }
            }

            JSONArray jsonArray = object.getJSONArray(pathArr[pathArr.length -1]);
            StringBuffer sb = new StringBuffer();
            if(null != jsonArray && jsonArray.size() > 0){
                ret = new ArrayList<T>();

                for(int i = 0;i<jsonArray.size();i++) {
                    object = (JSONObject) jsonArray.get(i);
                    T retObj = clazz.newInstance();
                    for (Field field : clazz.getDeclaredFields()) {
                        String name = field.getName();
                        if(name.indexOf("cn_mw_sign") >= 0){
                            continue;
                        }

                        String setMethodName = getSetMethodName(name);
                        if (null != nameMap) {
                            String changeName = nameMap.get(name);
                            if(StringUtils.isNotEmpty(changeName)){
                                name = changeName;
                            }
                        }


                        if (StringUtils.isNotEmpty(name)) {
                            String value = object.getString(name);
                            Method method = clazz.getDeclaredMethod(setMethodName ,String.class);
                            method.invoke(retObj, value);
                        }
                    }
                    ret.add(retObj);
                }
            }
        }
        return ret;
    }

    private static String getSetMethodName(String fildeName) {
        byte[] items = fildeName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        String ret = "set" + new String(items);
        return ret;
    }

    /**
     * 使用正则表达式删除字符串中的空格、回车、换行符、制表符
     * @param str
     * @return
     */
    public static String replaceAllBlank(String str) {
        String dest = "";
        if (StringUtils.isNotBlank(str)) {
            Matcher m = REPLACE_P.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
    /**
     * 去除字符串中的空格、回车、换行符、制表符
     *     \n 回车(\u000a)
     *     \t 水平制表符(\u0009)
     *     \s 空格(\u0008)
     *     \r 换行(\u000d)
     * @param source
     * @return
     */
    public static String replaceBlank(String source) {
        String ret = EMPTY;
        if (StringUtils.isNotBlank(source)) {
            ret = source.replaceAll(StringUtils.LF, EMPTY)
                    .replaceAll("\\s{2,}",  EMPTY)
                    .replaceAll("\\t", EMPTY)
                    .replaceAll(StringUtils.CR,  EMPTY);
        }
        return ret;
    }

    /**
     * 使用fastjson JSONObject格式化输出JSON字符串
     * @param source
     * @return
     */
    public static String formatJson(String source) {
        JSONObject object = JSONObject.parseObject(source);
        String pretty = JSON.toJSONString(object, SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        return pretty;
    }
    public static String formatJsonOneRow(String source) {
        JSONObject object = JSONObject.parseObject(source);
        String pretty = JSON.toJSONString(object, SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        return pretty;
    }

    public static void main(String[] args){
        String source = "[{\"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"indexId\": \"monitorMode\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": true,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 142,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"监控方式\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 16,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueList\": [],\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"manufacturer\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 158,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"厂商\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 17,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"indexId\": \"scanSuccessId\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": false,\n" +
                "        \"isLookShow\": false,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": false,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 153,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"资产扫描成功Id\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 17,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"indexId\": \"TPServerHostName\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": false,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 156,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"服务器主机名称\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 18,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueList\": [],\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"description\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 163,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"特征信息\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 19,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueList\": [],\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"specifications\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 159,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"规格型号\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 19,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueList\": [],\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"hostName\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 182,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"主机名称\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 20,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"indexId\": \"templateId\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": false,\n" +
                "        \"isLookShow\": false,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": false,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 171,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"模板Id\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 9,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 20,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"monitorServerId\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 172,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"Zabbix服务器\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 9,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 21,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"indexId\": \"assetsId\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": false,\n" +
                "        \"isLookShow\": false,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": false,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 175,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"监控资产Id\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 2,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 22,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"indexId\": \"hostGroupId\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": false,\n" +
                "        \"isLookShow\": false,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": false,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 177,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"群组id\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 2,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 23,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"0\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueList\": [],\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"monitorFlag\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 178,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"监控状态\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 17,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 24,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"0\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"isManage\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 179,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"是否纳管\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 17,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 25,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueList\": [],\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"sysObjId\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 371,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"OID\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 26,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"indexId\": \"pollingEngine\",\n" +
                "        \"isEditorShow\": true,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 407,\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"轮巡引擎\",\n" +
                "        \"propertiesType\": \"默认属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 27,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueList\": [],\n" +
                "        \"indexId\": \"modeOfAgent\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesId\": 28,\n" +
                "        \"propertiesLevel\": 1,\n" +
                "        \"propertiesName\": \"模式\",\n" +
                "        \"propertiesType\": \"默认属性\",\n" +
                "        \"propertiesTypeId\": 2,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 28,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"defaultValue\": \"\",\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueList\": [],\n" +
                "        \"indexId\": \"templateName\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"propertiesId\": 29,\n" +
                "        \"propertiesLevel\": 1,\n" +
                "        \"propertiesName\": \"模板名称\",\n" +
                "        \"propertiesType\": \"默认属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 29,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"defaultType\": 0,\n" +
                "        \"expireRemind\": false,\n" +
                "        \"gangedField\": \"\",\n" +
                "        \"gangedValueListStr\": \"\",\n" +
                "        \"indexId\": \"proxyServerId\",\n" +
                "        \"isEditorShow\": false,\n" +
                "        \"isGanged\": false,\n" +
                "        \"isInsertShow\": false,\n" +
                "        \"isListShow\": true,\n" +
                "        \"isLookShow\": true,\n" +
                "        \"isMust\": false,\n" +
                "        \"isOnly\": false,\n" +
                "        \"isRead\": false,\n" +
                "        \"isRelation\": false,\n" +
                "        \"isShow\": true,\n" +
                "        \"isStructInsert\": true,\n" +
                "        \"modelId\": 1003,\n" +
                "        \"modelIndex\": \"mw_6e7e65ef7cd54cd8a22f47301a421d5f\",\n" +
                "        \"modelName\": \"纳管资产父模型\",\n" +
                "        \"propertiesLevel\": 0,\n" +
                "        \"propertiesName\": \"代理服务器Id\",\n" +
                "        \"propertiesType\": \"纳管属性\",\n" +
                "        \"propertiesTypeId\": 1,\n" +
                "        \"regex\": \"\",\n" +
                "        \"relationModelIndex\": \"\",\n" +
                "        \"relationPropertiesIndex\": \"\",\n" +
                "        \"sort\": 21,\n" +
                "        \"timeUnit\": \"秒\"\n" +
                "    }\n" +
                "]";
    }

    public static Map<String, Object> parseJSON2Map(JSONObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 最外层解析
        for (Object k : json.keySet()) {
            Object v = json.get(k);
            // 如果内层还是json数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                Iterator<Object> it = ((JSONArray) v).iterator();  //迭代器不用考虑长度为0的情况
                while (it.hasNext()) {
                    JSONObject json2 = (JSONObject) it.next();
                    list.add(parseJSON2Map(json2));
                }
                map.put(k.toString(), list);
            } else if (v instanceof JSONObject) {
                // 如果内层是json对象的话，继续解析
                map.put(k.toString(), parseJSON2Map((JSONObject) v));
            } else {
                // 如果内层是普通对象的话，直接放入map中
                map.put(k.toString(), v);
            }
        }
        return map;
    }
}
