package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/28 21:57
 */
@Data
public class DiskTypeDto extends AssetsBaseDTO {
    @ApiModelProperty("磁盘/接口分区")
    private String type;
}
