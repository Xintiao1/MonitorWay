package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mwpaas.common.utils.BeansUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/8 18:02
 * 模型属性
 */
@Data
@ApiModel
public class PropertiesValueParam {

    @ApiModelProperty("主键id")
    private Integer id;
    @ApiModelProperty("属性类型id,1字符串,2整形数字,3浮点型数据,4布尔型,5日期类型,6结构体，7:Ip地址")
    private Integer propertiesValueType;
    @ApiModelProperty("显示类型（0默认，1多行字符串，2URL，3Markdown）")
    private Integer showType;
    @ApiModelProperty("属性默认值类型（0固定值，1内置函数，2自增Id，3流水号）")
    private Integer defaultType;
    @ApiModelProperty("属性默认值")
    private String defaultValue;
    @ApiModelProperty("正则表达式")
    private String regex;
    @ApiModelProperty("数组类数据")
    private List<String> dropOp;
    @ApiModelProperty("下拉框JSON数据")
    private List dropArrObj;
    @ApiModelProperty("数组类数据默认值")
    private List<String> defaultValueList;

    @ApiModelProperty("下拉框数据")
    private String dropOpStr;
    @ApiModelProperty("下拉框JSON数据")
    private String dropArrObjStr;
    @ApiModelProperty("下拉框数据默认值")
    private String defaultValueListStr;

    @ApiModelProperty("是否过期提醒")
    private Boolean expireRemind;
    @ApiModelProperty("过期时间数值")
    private Integer beforeExpireTime;
    @ApiModelProperty("过期时间单位:秒，分，时，天，月")
    private String timeUnit;

    @ApiModelProperty("评价周期0:仅一次，1:一年")
    private Integer judgeCycle;

    @ApiModelProperty("关联模型Index")
    private String relationModelIndex;

    @ApiModelProperty("关联属性Index")
    private String relationPropertiesIndex;

    @ApiModelProperty("是否关联模型数据")
    private Boolean isRelation;

    @ApiModelProperty("是否是关系联动字段")
    private Boolean isGanged;
    @ApiModelProperty("上级联动字段")
    //属性id+值 作为标记
    private List gangedValueList;

    //存入数据库时转字符串
    private String gangedValueListStr;

    //关联字段名称
    private String gangedField;

    public void extractFrom(PropertyInfo propertyInfo) {
        BeansUtils.copyProperties(propertyInfo, this);
        ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
        switch (type) {
            case SINGLE_ENUM:
            case MULTIPLE_ENUM:
                List list = (List) type.convertValue(propertyInfo.getPropertyValue());
                setDropOp(list);
                setDropOpStr(propertyInfo.getPropertyValue());
                list = (List) type.convertValue(propertyInfo.getDefaultValue());
                setDefaultValueList(list);
                setDefaultValueListStr(propertyInfo.getDefaultValue());
                break;
            case RELATION_ENUM:
                list = (List) type.convertValue(propertyInfo.getPropertyValue());
                setDropArrObj(list);
                setDropArrObjStr(propertyInfo.getPropertyValue());
        }
        setPropertiesValueType(type.getCode());
    }

}
