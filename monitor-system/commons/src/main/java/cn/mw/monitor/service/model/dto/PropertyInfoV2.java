package cn.mw.monitor.service.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ApiModel
@ToString
public class PropertyInfoV2 implements Comparable<PropertyInfoV2>{

    @ApiModelProperty("主键id")
    private Long propertiesId;

    @ApiModelProperty("es存的索引id")
    private String indexId;

    @ApiModelProperty("属性名称")
    private String propertiesName;

    @ApiModelProperty("属性类型id,1字符串 6结构体 7IP 8时间 9枚举型(单选) 10枚举型(多选) 11机构/部门 12负责人 13用户组 14附件(图片) 15附件(文件)")
    private Integer propertiesTypeId;

    @ApiModelProperty("属性分类（1:基本，2:默认，3:自定义）")
    private String propertiesType;

    @ApiModelProperty("属性分类信息，当属性分类为3自定义时，存入属性分类的信息。")
    private String propertiesTypeInfo;

    @ApiModelProperty("是否只读")
    private Boolean isRead;

    @ApiModelProperty("是否必填")
    private Boolean isMust;

    @ApiModelProperty("是否唯一")
    private Boolean isOnly;

    @ApiModelProperty("是否默认显示")
    private Boolean isShow;

    @ApiModelProperty("属性字段排序")
    private Integer sort = 0;

    @ApiModelProperty("是否结构体新增")
    private Boolean isStructInsert;

    @ApiModelProperty("是否新增显示")
    private Boolean isInsertShow;

    @ApiModelProperty("是否列表显示")
    private Boolean isListShow;

    @ApiModelProperty("是否查看显示")
    private Boolean isLookShow;

    @ApiModelProperty("是否修改显示")
    private Boolean isEditorShow;

    @ApiModelProperty("模型属性值信息")
    private String propertyValue;

    @ApiModelProperty("默认值")
    private String defaultValue;

    @ApiModelProperty("默认类型")
    private Integer defaultType;

    @ApiModelProperty("属性级别 0:内置属性，1:自定义属性,2:继承属性；其中内置属性不可删除")
    private Integer propertiesLevel;

    @ApiModelProperty("正则表达式")
    private String regex;

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

    @ApiModelProperty("是否是关系联动字段")
    private Boolean isGanged;
    @ApiModelProperty("上级联动字段")
    //属性id+值 作为标记
    private List gangedValueList;

    //存入数据库时转字符串
    private String gangedValueListStr;

    //关联字段名称
    private String gangedField;

    @ApiModelProperty("是否可以展开 ")
    private Integer isTree;

    @Override
    public int compareTo(PropertyInfoV2 o) {
        return sort - o.getSort();
    }

}
