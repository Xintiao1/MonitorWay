package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.service.model.param.PropertiesValueParam;
import cn.mw.monitor.service.model.dto.ModelLevel;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/19 9:57
 */
@Data
@ToString
public class ModelPropertiesDto {
    @ApiModelProperty("主键id")
    private Integer propertiesId;

    @ApiModelProperty("es存的索引")
    private String indexId;

    @ApiModelProperty("属性名称")
    private String propertiesName;

    @ApiModelProperty("默认值")
    private String defaultValue;

    @ApiModelProperty("属性值类型名称")
    private String propertiesTypeName;

    @ApiModelProperty("属性值类型Id")
    private Integer propertiesTypeId;

    @ApiModelProperty("属性分类（基本，默认）")
    private String propertiesType;

    @ApiModelProperty("模型id")
    private Integer modelId;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("是否只读")
    private Boolean isRead;

    @ApiModelProperty("是否必填")
    private Boolean isMust;

    @ApiModelProperty("是否唯一")
    private Boolean isOnly;

    @ApiModelProperty("是否显示")
    private Boolean isShow;

    @ApiModelProperty("是否纳管字段")
    private Integer isManage;

    @ApiModelProperty("是否新增结构体，true新增，false导入")
    private Boolean isStructInsert;

    @ApiModelProperty("是否新增显示")
    private Boolean isInsertShow;

    @ApiModelProperty("是否列表显示")
    private Boolean isListShow;

    @ApiModelProperty("是否查看显示")
    private Boolean isLookShow;

    @ApiModelProperty("是否修改显示")
    private Boolean isEditorShow;

    @ApiModelProperty("属性字段排序")
    private Integer sort;

    @ApiModelProperty("属性级别 0:内置属性，1:自定义属性,2:继承父模型属性;其中内置属性不可删除")
    private Integer propertiesLevel;

    @ApiModelProperty("属性级别名称")
    private String propertiesLevelName;

    @ApiModelProperty("模型属性结构体")
    private List<ModelPropertiesStructDto> propertiesStruct;

    @ApiModelProperty("模型属性值信息")
    private PropertiesValueParam propertiesValue;

    @ApiModelProperty("是否模型关联数据")
    private Boolean isRelation;

    @ApiModelProperty("是否是关系联动字段")
    private Boolean isGanged;
    @ApiModelProperty("上级联动字段")
    //属性id+值 作为标记
    private List gangedValueList;
    @ApiModelProperty("是否告警字段 ")
    private Boolean alertField;
    //存入数据库时转字符串
    private String gangedValueListStr;

    //关联字段名称
    private String gangedField;

    public void extractFrom(PropertyInfo propertyInfo){
        BeanUtils.copyProperties(propertyInfo ,this);

        PropertiesValueParam propertiesValueParam = new PropertiesValueParam();
        propertiesValueParam.extractFrom(propertyInfo);

        setDefaultValue(propertyInfo.getDefaultValue());
        ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
        this.propertiesTypeName = type.getType();
        switch (type) {
            case STRUCE:
                List list = (List) type.convertValue(propertyInfo.getPropertyValue());
                setPropertiesStruct(list);
        }
        this.setPropertiesValue(propertiesValueParam);

        if(null != propertyInfo.getPropertiesLevel()) {
            ModelLevel modelLevel = ModelLevel.valueOf(propertyInfo.getPropertiesLevel());
            if (null != modelLevel) {
                setPropertiesLevelName(modelLevel.getDesc());
            }
        }
    }
}
