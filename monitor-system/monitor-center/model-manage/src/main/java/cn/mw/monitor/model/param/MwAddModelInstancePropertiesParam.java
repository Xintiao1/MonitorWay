package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author xhy
 * @date 2021/3/1 16:53
 */
@Data
@ApiModel
public class MwAddModelInstancePropertiesParam  {
    @ApiModelProperty("资产的主键Id")
    private String tangibleId;
    @ApiModelProperty("模型的索引")
    private String modelIndex;
    @ApiModelProperty("模型的索引的Id")
    @Size(max=32,message = "最大长度不能超过32")
    private String modelIndexId;
    @ApiModelProperty("模型的Id")
    private Integer modelId;
    @ApiModelProperty("模型的实例Id")
    private Integer modelInstanceId;

    @ApiModelProperty("模型的属性和属性值")
    List<AddModelInstancePropertiesParam> propertiesList;
}
