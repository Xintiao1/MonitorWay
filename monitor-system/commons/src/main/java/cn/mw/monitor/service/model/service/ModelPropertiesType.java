package cn.mw.monitor.service.model.service;

import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.mw.monitor.service.model.util.ValConvertUtil.doubleValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

/**
 * @author qzg
 * @date 2022/8/26
 */
public enum ModelPropertiesType {
    STRING(1, "字符串", "1", null, null),

    INTEGER(2, "整形数字", "1", new PropertyTypeConvert<Integer, Integer>() {
        @Override
        public Integer convert(String value) {
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            return Integer.parseInt(value);
        }

        @Override
        public String strValue(Integer value) {
            return value.toString();
        }

        @Override
        public boolean matchType(Object obj) {
            return obj instanceof Integer;
        }
    }, null),
    CONNECT_RELATION(3, "关系关联", "1", null, null),

    MULTIPLE_RELATION(4, "外部关联(多选)", "6", null, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            return JSONArray.parse(value);
        }
    }),
    SINGLE_RELATION(5, "外部关联(单选)", "6", null, null),
    STRUCE(6, "结构体", "1", new PropertyTypeConvert<List<ModelPropertiesStructDto>, List<ModelPropertiesStructDto>>() {
        @Override
        public List<ModelPropertiesStructDto> convert(String value) {
            List<ModelPropertiesStructDto> ret = new ArrayList<>();
            if (!Strings.isNullOrEmpty(value) && !"null".equals(value)) {
                ret = JSONArray.parseArray(value, ModelPropertiesStructDto.class);
                for (ModelPropertiesStructDto modelPropertiesStructDto : ret) {
                    ModelPropertiesType structType = typeMap.get(modelPropertiesStructDto.getStructType());
                    switch (structType) {
                        case SINGLE_ENUM:
                        case MULTIPLE_ENUM:
                            Object obj = structType.converter.convert(modelPropertiesStructDto.getStructStrValue());
                            modelPropertiesStructDto.setStructListValue((List) obj);
                    }
                }
            }
            return ret;
        }

        @Override
        public String strValue(List<ModelPropertiesStructDto> value) {
            return JSON.toJSONString(value);
        }
    }, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            return JSONArray.parse(value);
        }
    }),
    IP(7, "IP", "1", null, null),
    DATE(8, "时间", "2", new PropertyTypeConvert<Date, Date>() {
        @Override
        public Date convert(String value) {
            Date ret = null;
            try {
                ret = dateFormat.parse(value);
            } catch (Exception e) {

            }
            return ret;
        }

        @Override
        public boolean matchType(Object obj) {
            return obj instanceof Date;
        }

        @Override
        public String strValue(Date value) {
            return dateFormat.format(value);
        }
    }, null),

    SINGLE_ENUM(9, "枚举型(单选)", "6", new PropertyTypeConvert<List<String>, List<String>>() {
        @Override
        public List<String> convert(String value) {
            return Arrays.asList(value.split(SEP));
        }

        @Override
        public String strValue(List<String> value) {
            return strFromStringList(value);
        }
    }, null),
    MULTIPLE_ENUM(10, "枚举型(多选)", "6", new PropertyTypeConvert<List<String>, List<String>>() {
        @Override
        public List<String> convert(String value) {
            return Arrays.asList(value.split(SEP));
        }

        @Override
        public String strValue(List<String> value) {
            return strFromStringList(value);
        }
    }, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            return JSONArray.parse(value);
        }
    }),
    ORG(11, "机构/部门", "6", null, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            return JSONArray.parse(value);
        }
    }),
    USER(12, "负责人", "6", null, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            return JSONArray.parse(value);
        }
    }),
    GROUP(13, "用户组", "6", null, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            return JSONArray.parse(value);
        }
    }),
    ENCLOSURE_IMG(14, "附件(图片)", "1", null, null),
    ENCLOSURE_FILE(15, "附件(文件)", "1", null, null),
    LAYOUTDATA(16, "布局位置", "1", null, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            return JSONArray.parse(value);
        }
    }),
    SWITCH(17, "开关型", "1", null, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            return Boolean.valueOf(value);
        }
    }),
    MONITORSERVER_RELATION(18, "监控服务关联", "6", null, null),
    PASSWORD(19, "密文型", "1", null, null),
    RELATION_ENUM(20, "枚举型(联动)", "1", new PropertyTypeConvert<List, List<JSONObject>>() {
        @Override
        public List convert(String value) {
            return JSONArray.parseArray(value);
        }

        @Override
        public String strValue(List<JSONObject> value) {
            return value.toString();
        }
    }, null),
    JUDGE(21, "评价字段", "1", null, null),
    MONEY(22, "金额", "1", null, new EsConverter() {
        @Override
        public Object convertToESData(String value) {
            double moneyNum = doubleValueConvert(value);
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            String numStr = decimalFormat.format(moneyNum);
            return numStr;
        }
    }),
    LONGTEXT(23, "文本输入", "1", null, null);

    private static final String SEP = ",";
    private int code;
    private String type;

    //1:文本 2:时间 6:下拉框
    private String inputFormat;

    private PropertyTypeConvert converter;
    private EsConverter esConverter;
    private static Map<Integer, ModelPropertiesType> typeMap;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        typeMap = new HashMap<>();
        for (ModelPropertiesType type : ModelPropertiesType.values()) {
            typeMap.put(type.code, type);
        }
    }

    ModelPropertiesType(int code, String type, String inputFormat, PropertyTypeConvert converter, EsConverter esConverter) {
        this.code = code;
        this.type = type;
        this.inputFormat = inputFormat;

        if (null == converter) {
            this.converter = new PropertyTypeConvert<String, String>() {
                @Override
                public String convert(String value) {
                    return value;
                }

                @Override
                public String strValue(String value) {
                    return value;
                }
            };
        } else {
            this.converter = converter;
        }

        this.esConverter = esConverter;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public PropertyTypeConvert getConverter() {
        return this.converter;
    }

    public static String strFromStringList(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : list) {
            stringBuffer.append(str).append(SEP);
        }
        return stringBuffer.toString().substring(0, stringBuffer.length() - 1);
    }

    public static ModelPropertiesType getTypeByCode(Integer code) {
        if (null == code) {
            return STRING;
        }
        return typeMap.get(code);
    }

    public Object convertValue(int type, String value) {
        ModelPropertiesType modelPropertiesType = typeMap.get(type);
        return modelPropertiesType.converter.convert(value);
    }

    public Object convertValue(String value) {
        if (null == value) {
            return null;
        }
        return this.converter.convert(value);
    }

    public Object convertToEsData(String value) {
        if (null == esConverter) {
            return value;
        }
        return this.esConverter.convertToESData(value);
    }
}