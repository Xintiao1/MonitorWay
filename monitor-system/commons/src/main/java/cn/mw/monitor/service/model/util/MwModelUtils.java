package cn.mw.monitor.service.model.util;

import cn.mw.monitor.service.assets.model.ESStructData;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.param.AddAndUpdateModelPropertiesParam;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import cn.mw.monitor.service.model.param.DateParam;
import cn.mw.monitor.service.model.param.PropertiesValueParam;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.model.service.PropertyTypeConvert;
import cn.mw.monitor.util.ListMapObjUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

@Slf4j
public class MwModelUtils {
    private static final String PROPERTIES = "properties";
    private static Map<Class, Map<String, String>> classFieldNameMap = new HashMap<>();

    static {
        Map<String, String> mwTangibleassetsDTOMap = new HashMap<>();
        mwTangibleassetsDTOMap.put("modelViewUserIds", "userIds");
        mwTangibleassetsDTOMap.put("modelViewOrgIds", "orgIds");
        mwTangibleassetsDTOMap.put("modelViewGroupIds", "groupIds");
        mwTangibleassetsDTOMap.put("snmpv1AssetsDTO", MwModelViewCommonService.SNMPV1V2);
        mwTangibleassetsDTOMap.put("snmpAssetsDTO", MwModelViewCommonService.SNMPV3);
        mwTangibleassetsDTOMap.put("agentAssetsDTO", MwModelViewCommonService.ZABBIX_AGENT);
        mwTangibleassetsDTOMap.put("portAssetsDTO", MwModelViewCommonService.ICMP);
        mwTangibleassetsDTOMap.put("id", MwModelViewCommonService.INSTANCE_ID_KEY);
        mwTangibleassetsDTOMap.put("assetsName", MwModelViewCommonService.INSTANCE_NAME_KEY);
        classFieldNameMap.put(MwTangibleassetsDTO.class, mwTangibleassetsDTOMap);
        classFieldNameMap.put(MwTangibleassetsTable.class, mwTangibleassetsDTOMap);
    }

    public static void copyProperties(AddAndUpdateModelPropertiesParam propertiesParam, PropertyInfo propertyInfo) {
        BeanUtils.copyProperties(propertiesParam, propertyInfo);
        BeanUtils.copyProperties(propertiesParam.getPropertiesValue(), propertyInfo);
        ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
        String value = null;
        String defaultValue = null;
        PropertiesValueParam propertiesValueParam = propertiesParam.getPropertiesValue();
        switch (type) {
            case SINGLE_ENUM:
                value = type.getConverter().strValue(propertiesValueParam.getDropOp());
                defaultValue = propertiesValueParam.getDefaultValue();
                break;
            case MULTIPLE_ENUM:
                value = type.getConverter().strValue(propertiesValueParam.getDropOp());
                defaultValue = type.getConverter().strValue(propertiesValueParam.getDefaultValueList());
                break;
            case RELATION_ENUM:
                value = propertiesValueParam.getDropArrObj().toString();
                defaultValue = propertiesValueParam.getDefaultValue();
                break;
            case STRUCE:
                value = type.getConverter().strValue(propertiesParam.getPropertiesStruct());
                break;
            default:
                String pValue = propertiesParam.getPropertyValue();
                String pDefaultValue = propertiesValueParam.getDefaultValue();
                PropertyTypeConvert convert = type.getConverter();
                if (null != pValue && null != convert) {
                    if (convert.matchType(pValue)) {
                        value = convert.strValue(pValue);
                    } else {
                        value = pValue;
                    }
                }

                if (null != pDefaultValue && null != convert) {
                    if (convert.matchType(pDefaultValue)) {
                        defaultValue = convert.strValue(pDefaultValue);
                    } else {
                        defaultValue = pDefaultValue;
                    }
                }
        }

        if (propertiesValueParam.getGangedValueList() != null && propertiesValueParam.getGangedValueList().size() > 0) {
            //最后加入空字符串，用户数据库like查询方便。
            propertiesValueParam.getGangedValueList().add("");
            String str = JSONArray.toJSONString(propertiesValueParam.getGangedValueList());
            propertyInfo.setGangedValueListStr(str);
        }

        propertyInfo.setPropertyValue(value);
        propertyInfo.setDefaultValue(defaultValue);
    }

    public static QueryBuilder tranformEsQuery(String itemName, Object data) {
        QueryBuilder ret = null;
        if (data instanceof Integer) {
            ret = QueryBuilders.termQuery(itemName, Integer.class.cast(data));
        } else if (data instanceof String) {
            ret = QueryBuilders.termQuery(itemName, data.toString());
        } else if (data instanceof DateParam) {
            DateParam param = (DateParam) data;
            Date startTime = param.getStartTime();
            if (null == startTime) {
                startTime = DateUtils.parse("1970-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
            }

            Date endTime = param.getEndTime();
            if (null == endTime) {
                endTime = DateUtils.parse("2999-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
            }
            ret = QueryBuilders.rangeQuery(itemName).from(startTime).to(endTime);
        }
        return ret;
    }

    public static QueryBuilder tranformEsQuery(AddModelInstancePropertiesParam param) {
        QueryBuilder ret = null;
        if (!Strings.isNullOrEmpty(param.getPropertiesValue())) {
            if (param.getPropertiesType() != null) {
                ModelPropertiesType type = ModelPropertiesType.getTypeByCode(param.getPropertiesType());
                switch (type) {
                    case DATE:
                        Date startTime = param.getStartTime();
                        if (null == startTime) {
                            startTime = DateUtils.parse("1970-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
                        }

                        Date endTime = param.getEndTime();
                        if (null == endTime) {
                            endTime = DateUtils.parse("2999-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
                        }
                        ret = QueryBuilders.rangeQuery(param.getPropertiesIndexId()).from(startTime).to(endTime);
                        break;
                    case STRING:
                    case SINGLE_RELATION:
                    case IP:
                    case SINGLE_ENUM:
                        String value = param.getPropertiesValue().replace("*", "\\*").replace("?", "\\?");
                        ret = QueryBuilders.wildcardQuery(param.getPropertiesIndexId() + ".keyword", "*" + value + "*".toLowerCase());
//                        ret = QueryBuilders.matchQuery(param.getPropertiesIndexId(), value).analyzer("lowercase");
                        if (param.isFilterQuery()) {//是否精准查询
                            ret = QueryBuilders.termQuery(param.getPropertiesIndexId(), param.getPropertiesValue());
                        }
                        break;
                    case ORG:
                    case USER:
                    case GROUP:
                    case INTEGER:
                    case MULTIPLE_ENUM:
                    case MULTIPLE_RELATION:
                    case LAYOUTDATA:
                        ret = QueryBuilders.termQuery(param.getPropertiesIndexId(), param.getPropertiesValue());
                        break;
                    case SWITCH:
                        ret = QueryBuilders.termQuery(param.getPropertiesIndexId(), Boolean.parseBoolean(param.getPropertiesValue()));
                        break;
                    case STRUCE:
                        ret = QueryBuilders.nestedQuery(param.getPropertiesIndexId()
                                , QueryBuilders.matchPhraseQuery(param.getPropertiesIndexId()
                                        + "." + param.getPropertiesInstanceStruct(), param.getPropertiesValue()), ScoreMode.None);
                        break;
                }
            }

        }
        return ret;
    }

    public static <T> List<T> convertEsData(Class clazz
            , List<Map<String, Object>> mapList) throws Exception {

        List ret = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Map<String, Class> typeMap = new HashMap<>();
        Map<String, Method> methodMap = new HashMap<>();
        List<Field> fields = new ArrayList<>();
        getListWithParent(clazz, fields);

        Map<String, String> nameMap = classFieldNameMap.get(clazz);
        if (null == nameMap) {
            nameMap = new HashMap<>();
        }
        for (Field field : fields) {
            String name = field.getName();
            String mapName = nameMap.get(name);

            if (null != mapName) {
                name = mapName.toLowerCase();
            } else {
                name = name.toLowerCase();
            }

            typeMap.put(name, field.getType());
            String setMethod = StringUtils.genSetMethoName(field.getName());
            Method method = clazz.getMethod(setMethod, field.getType());
            methodMap.put(name, method);
        }

        for (Map<String, Object> map : mapList) {
            Object destObj = clazz.newInstance();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                try {
                    String key = entry.getKey().toLowerCase();
                    Class type = typeMap.get(key);
                    Object value = entry.getValue();
                    if (null != value && null != type) {
                        Method method = methodMap.get(key);
                        if (type == String.class) {
                            method.invoke(destObj, value.toString());
                        } else if (type == Integer.class || type == int.class) {
                            if (StringUtils.isNotEmpty(value.toString())) {
                                int valueInt = Integer.parseInt(value.toString());
                                method.invoke(destObj, valueInt);
                            }
                        } else if (type == Date.class) {
                            if (StringUtils.isBlank(String.valueOf(value))) {
                                continue;
                            }
                            //常见时间格式转换
                            Date date = isCheckDateFormat(strValueConvert(value));
                            if (date == null) {
                                //CST格式转换
                                date = isCheckDateCSTFormat(strValueConvert(value));
                            }
                            if (date == null) {
                                date = new Date();
                            }
                            method.invoke(destObj, date);
                        } else if (type == Boolean.class || type == boolean.class) {
                            boolean valueB = Boolean.parseBoolean(value.toString());
                            method.invoke(destObj, valueB);
                        } else if (List.class.isAssignableFrom(type)) {
                            method.invoke(destObj, value);
                        } else if (ESStructData.class.isAssignableFrom(type)) {
                            if (null != value && value instanceof List) {
                                List valueList = (List) value;
                                if (valueList.size() > 0) {
                                    Object obj = ListMapObjUtils.mapToBean((Map) valueList.get(0), type);
                                    method.invoke(destObj, type.cast(obj));
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
            ret.add(destObj);
        }

        return ret;
    }

    private static Date isCheckDateCSTFormat(String dateStr) {
        SimpleDateFormat input = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        try {
            Date parse = input.parse(dateStr);
            return parse;
        } catch (ParseException e) {
            return null;
        }
    }

    private static Date isCheckDateFormat(String dateStr) {
        Date parse = DateUtils.parse(dateStr);
        return parse;
    }

    private static void getListWithParent(Class clazz, List<Field> list) {
        list.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class parent = clazz.getSuperclass();
        if (parent.getCanonicalName().indexOf("cn.mw.monitor") >= 0) {
            getListWithParent(parent, list);
        }
    }

    public static List<String> getNotExistFields(String index, Set<String> checkFields
            , RestHighLevelClient restHighLevelClient) throws Exception {
        List<String> ret = new ArrayList<>();

        if (null != checkFields) {
            GetMappingsRequest getMappingsRequest = new GetMappingsRequest();
            getMappingsRequest.indices(index);

            GetMappingsResponse response = restHighLevelClient.indices().getMapping(getMappingsRequest, RequestOptions.DEFAULT);
            Map<String, MappingMetadata> mappingsByIndex = response.mappings();
            MappingMetadata mappingMetadata = mappingsByIndex.get(index);
            Map fieldMap = (Map) mappingMetadata.getSourceAsMap().get("properties");

            if (null == fieldMap) {
                ret.addAll(checkFields);
            } else {
                for (String field : checkFields) {
                    if (null == fieldMap.get(field)) {
                        ret.add(field);
                    }
                }
            }
        }

        return ret;
    }

    private static Map<String, Object> getESMappingByModelId(String modelIndex, RestHighLevelClient restHighLevelClient) throws Exception {
        Map<String, Object> m = new HashMap<>();
        //获取mapping文件
        GetMappingsRequest request = new GetMappingsRequest();
        request.indices(modelIndex);
        GetMappingsResponse getMappingsResponse = restHighLevelClient.indices().getMapping(request, RequestOptions.DEFAULT);
        MappingMetadata mappings = getMappingsResponse.mappings().get(modelIndex);
        if (mappings != null) {
            Map<String, Object> mapping = (Map<String, Object>) mappings.getSourceAsMap();
            if (mappings != null) {
                m = (Map<String, Object>) mapping.get(PROPERTIES);
            }
        }
        return m;
    }

    public static void batchSetESMapping(String index, List<String> addFields
            , Map<String, AddModelInstancePropertiesParam> fidldMap, RestHighLevelClient restHighLevelClient) {

        try {
            Map<String, Object> esMappingInfo = getESMappingByModelId(index, restHighLevelClient);
            Map properties = new HashMap();
            Map fields = new HashMap();
            for (String field : addFields) {
                AddModelInstancePropertiesParam param = fidldMap.get(field);
                ModelPropertiesType type = ModelPropertiesType.getTypeByCode(param.getPropertiesType());
                Map fieldDefinition = null;
                switch (type) {
                    case STRING:
                        fieldDefinition = doESMappingByString();
                        break;
                    case DATE:
                        fieldDefinition = doESMappingByDate();
                        break;
                    case SWITCH:
                        fieldDefinition = doESMappingByBoolean();
                        break;
                    case STRUCE:
                        fieldDefinition = doESMappingByStruct();
                        break;
                    default:
                }
                if (esMappingInfo!=null && !esMappingInfo.containsKey(field)) {
                    fields.put(field, fieldDefinition);
                }

            }
            if (fields != null && !CollectionUtils.isEmpty(fields)) {
                properties.put("properties", fields);
                PutMappingRequest request = new PutMappingRequest(index);
                request.source(JSONObject.toJSONString(properties), XContentType.JSON);
                restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
            }
        } catch (Exception e) {
            log.error("batchSetESMapping",e);
            throw new RuntimeException(e);
        }
    }

    private static Map doESMappingByString() {
        Map value = new HashMap();
        Map fields = new HashMap();
        Map type = new HashMap();
        value.put("type", "text");
        value.put("fields", fields);
        fields.put("keyword", type);
        type.put("type", "keyword");
        type.put("normalizer", "my_analyzer");
        return value;
    }

    private static Map doESMappingByDate() {
        Map value = new HashMap();
        value.put("type", "date");
        value.put("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
        return value;
    }

    private static Map doESMappingByBoolean() {
        Map value = new HashMap();
        value.put("type", "boolean");
        return value;
    }

    private static Map doESMappingByStruct() {
        Map value = new HashMap();
        value.put("type", "nested");
        return value;
    }

    public static boolean isSNMPType(int monitorMode) {
        RuleType ruleType = RuleType.getInfoByMonitorMode(monitorMode);
        if (null != ruleType
                && (RuleType.SNMPv1v2 == ruleType || RuleType.SNMPv3 == ruleType)) {
            return true;
        }
        return false;
    }

    /*
    public static void main(String[] args){
        List<QueryModelParam> datas = new ArrayList<>();
        QueryModelExist queryModelExist = new QueryModelExist("test1");
        datas.add(queryModelExist);
        queryModelExist = new QueryModelExist("test2");
        datas.add(queryModelExist);

        List<Integer> sub = new ArrayList<>();
        sub.add(1);
        sub.add(2);
        QueryModelParam k = new QueryModelAnd("test" ,sub);
        datas.add(k);

        QueryModelParam queryModelParam = new QueryModelAnd("test" ,datas);
        ModelQuery modelQuery = ModelQueryFactory.genModelQuery(queryModelParam);
        QueryBuilder queryBuilder = modelQuery.genQuery();
        System.out.println(queryBuilder.toString());
    }

     */
}
