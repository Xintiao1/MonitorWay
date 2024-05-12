package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.param.PropertiesValueParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/8 18:02
 * 模型属性
 *
 */
@Data
@ApiModel
public class AddAndUpdateModelPropertiesParam extends PropertyInfo {
    @ApiModelProperty("模型id")
    private Integer modelId;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("es存的索引")
    private String modelIndex;

    @ApiModelProperty("主键idList")
    private List<Integer> propertiesIdList;

    @ApiModelProperty("属性名称List")
    private List<String> propertiesNameList;

    @ApiModelProperty("模型属性结构体")
    private List<ModelPropertiesStructDto> propertiesStruct;

    @ApiModelProperty("模型属性值信息")
    private PropertiesValueParam propertiesValue;

    @ApiModelProperty("模型属性值Id")
    private Integer propertiesValueId;
}
