package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/23
 */
@Data
public class MWModelInstancePropertiesFiledDTO {
    //模型实例属性id
    private String id;
    //模型实例属性名称
    private String name;
    //模型实例属性类型
    private Integer type;
    //模型属性值
    private Object value;
    //模型属性 默认值（多选）
    private List dataArrObj;
    //模型属性结构体
    private List<ModelPropertiesStructDto> propertiesStruct;

}
