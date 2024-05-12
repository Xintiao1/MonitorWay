package cn.mw.monitor.logManage.param;

import cn.mw.monitor.logManage.dto.DictionaryDetailDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@ApiModel(value = "解析字典参数")
public class DictionaryAddParam {

    private Integer id;

    @ApiModelProperty(value = "字典名称")
    private String dictionaryName;

    @ApiModelProperty(value = "字典类型")
    private String dictionaryType;

    @ApiModelProperty(value = "字典描述")
    private String dictionaryDesc;

    @ApiModelProperty(value = "字典明细")
    private List<DictionaryDetailDTO> dictionaryDetailList;

    public void validation() throws IllegalArgumentException {
        if (StringUtils.isBlank(dictionaryName)) {
            throw new IllegalArgumentException("模型名称为空");
        }
        if (StringUtils.isBlank(dictionaryType)) {
            throw new IllegalArgumentException("模型类型为空");
        }
        if (CollectionUtils.isEmpty(dictionaryDetailList)) {
            throw new IllegalArgumentException("模型明细为空");
        }
    }
}
