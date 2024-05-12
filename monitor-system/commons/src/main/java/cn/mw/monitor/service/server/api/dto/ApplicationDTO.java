package cn.mw.monitor.service.server.api.dto;

/**
 * @author qzg
 * @date 2021/7/2
 */

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 高级表格查询
 *
 * @author qzg
 * @Date 2021/6/20
 */
@Data
public class ApplicationDTO {
    @ApiModelProperty("应用集名称")
    private String applicationName;
    @ApiModelProperty("监控项名称")
    private List<String> itemNames;
    @ApiModelProperty("监控主体名称")
    private List<String> devNames;
    @ApiModelProperty("自定义接口名称")
    private String interfaceName;
}
