package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/23
 */
@Data
public class MWModelPropertiesGangedDto {
    //
    private String label;
    //
    private String value;

    private List children;

    public void extractFromPropertyInfo(PropertyInfo propertyInfo){
        this.label = propertyInfo.getPropertiesName();
        this.value = propertyInfo.getIndexId();

        ModelPropertiesType type = ModelPropertiesType.getTypeByCode(propertyInfo.getPropertiesTypeId());
        this.children = (List)type.getConverter().convert(propertyInfo.getPropertyValue());
    }
}
