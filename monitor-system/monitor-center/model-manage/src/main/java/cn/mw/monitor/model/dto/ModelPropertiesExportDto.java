package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 模型属性结构体
 *
 * @author qzg
 * @date 2021/10/11
 */
@Data
public class ModelPropertiesExportDto {
    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("模型属性名称")
    private String propertiesName;

    @ApiModelProperty("模型属性类型")
    private String propertiesType;

    @ApiModelProperty("是否只读")
    private Boolean isRead;

    @ApiModelProperty("是否必填")
    private Boolean isMust;

    @ApiModelProperty("是否唯一")
    private Boolean isOnly;

    @ApiModelProperty("是否显示")
    private Boolean isShow;
    @ApiModelProperty("模型属性默认值")
    private String dropOpValue;

    @ApiModelProperty("模型Id")
    private Integer modelId;

    @ApiModelProperty("模型IndexId")
    private String modelIndexId;

    @ApiModelProperty("模型属性IndexId")
    private String propertiesIndexId;

    @ApiModelProperty("外部关联模型Index")
    private String relationModelIndex;

    @ApiModelProperty("模型视图")
    private Integer modelView;

    public void extractFrom(PropertyInfo propertyInfo) {
        setPropertiesIndexId(propertyInfo.getIndexId());
        setDropOpValue(propertyInfo.getPropertyValue());
        setPropertiesType(propertyInfo.getPropertiesTypeId()+"");
        setPropertiesName(propertyInfo.getPropertiesName());
        setRelationModelIndex(propertyInfo.getRelationModelIndex());
        setIsShow(propertyInfo.getIsShow());
        setIsRead(propertyInfo.getIsRead());
        setIsMust(propertyInfo.getIsMust());
        setIsOnly(propertyInfo.getIsOnly());
        ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
        switch (type) {
            case STRING:
            case IP:
                setDropOpValue(propertyInfo.getRegex());
                break;
            case SINGLE_ENUM:
            case MULTIPLE_ENUM:
                setDropOpValue(propertyInfo.getPropertyValue());
        }
    }


}
