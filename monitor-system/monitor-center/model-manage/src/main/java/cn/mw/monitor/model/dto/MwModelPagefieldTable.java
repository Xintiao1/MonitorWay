package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MwModelPagefieldTable {

    // id
    private Integer id;
    // modelID
    private Integer modelId;
    private Integer modelPropertiesId;
    // 字段代码
    private String prop;
    // 字段名称
    private String label;
    // 是否重要字段
    private Boolean importance;
    private Integer orderNum;

    //字段显示的类型
    // 1	字符串
    //6	结构体
    //8	时间
    //9	枚举型(单选)
    //10	多选枚举型
    //11	机构/部门
    //12	负责人
    //13	用户组
    private String type;

    private String isRead;
    private String isMust;
    private String isOnly;

    //属性正则表达式
    private String regex;
    //属性默认值
    private String defaultValue;

    @ApiModelProperty("数组类数据")
    private List<String> dropOp;
    @ApiModelProperty("数组类数据默认值")
    private List<String> defaultValueList;

    @ApiModelProperty("下拉框数据")
    private String dropOpStr;
    @ApiModelProperty("下拉框数据默认值")
    private String defaultValueListStr;

    private String propertiesType;

    @ApiModelProperty("是否过期提醒")
    private Boolean expireRemind;
    @ApiModelProperty("过期时间数值")
    private Integer beforeExpireTime;
    @ApiModelProperty("过期时间单位:秒，分，时，天，月")
    private String timeUnit;

    @ApiModelProperty("关联模型Index")
    private String relationModelIndex;

    @ApiModelProperty("关联属性Index")
    private String relationPropertiesIndex;

    @ApiModelProperty("是否关联模型数据")
    private Boolean isRelation;

}
