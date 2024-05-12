package cn.mw.monitor.webMonitor.api.param.webMonitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/5/11
 */
@Data
@Builder
public class AddMonitorItemParam {
    @ApiModelProperty("zibbix返回的ID")
    private Integer monitorId;

    @ApiModelProperty("zibbix返回的itemID")
    private String itemId;

    @ApiModelProperty("zibbix返回的itemName")
    private String itemName;
}
