package cn.mw.monitor.server.serverdto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 高级表格字段数据DTO
 * @author qzg
 * @date 2021/7/6
 */
@Data
public class AdvanceTableFieldDTO {
    @ApiModelProperty("应用集名称")
    private String applicationName;
    @ApiModelProperty("监控项名称")
    private String itemName;
    @ApiModelProperty("监控主体名称")
    private String partName;
    @ApiModelProperty("监控值")
    private String value;
}
