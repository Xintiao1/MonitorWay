package cn.mw.monitor.logManage.param;

import cn.mw.monitor.logManage.dto.ModelDetailDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@ApiModel(value = "模型请求实体")
public class ModelAddParam {

    @ApiModelProperty(value = "添加数据不用传递，更新时使用")
    private Integer id;

    @ApiModelProperty(value = "模型名称")
    private String modelName;

    @ApiModelProperty(value = "模型类型")
    private String modelType;

    @ApiModelProperty(value = "模型明细")
    private List<ModelDetailDTO> modelDetailList;

    public void validation() throws IllegalArgumentException {
        if (StringUtils.isBlank(modelName)) {
            throw new IllegalArgumentException("模型名称为空");
        }
        if (StringUtils.isBlank(modelType)) {
            throw new IllegalArgumentException("模型类型为空");
        }
        if (CollectionUtils.isEmpty(modelDetailList)) {
            throw new IllegalArgumentException("模型明细为空");
        }
    }
}
