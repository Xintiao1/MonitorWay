package cn.mw.monitor.logManage.param;

import cn.mw.monitor.logManage.dto.DataProcessingDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(description = "解析库数据处理入参")
@Data
public class ParsingLibProcessingParam {

    @ApiModelProperty(value = "解析库ID")
    private Integer id;

    @ApiModelProperty(value = "数据处理的参数")
    private List<DataProcessingDTO> paramList;
}
