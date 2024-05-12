package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/11/26 17:01
 * @Version 1.0
 */
@Data
public class ServerDTO extends AssetsBaseDTO {
    //    监控项名称
    @ApiModelProperty("监控项名称")
    private List<String> name;
    @ApiModelProperty("监控项Id")
    private List<String> itemId;
}
