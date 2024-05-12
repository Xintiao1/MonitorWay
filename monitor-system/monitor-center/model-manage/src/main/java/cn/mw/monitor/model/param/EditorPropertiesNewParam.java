package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.dto.ModelPropertiesStructDto;
import cn.mw.monitor.service.model.dto.PropertyInfo;
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
public class EditorPropertiesNewParam  extends PropertyInfo{
    @ApiModelProperty("模型id")
    private Integer modelId;

    @ApiModelProperty("属性Index")
    private List<String> propertiesIndexIds;

    @ApiModelProperty("属性名称List")
    private List<String> propertiesNameList;

    @ApiModelProperty("模型名称")
    private String modelName;

}
