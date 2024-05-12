package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/28 14:57
 */
@Data
public class DiskListDto {
    @ApiModelProperty("分区类型")
    private String type;
    @ApiModelProperty("总容量")
    private String diskTotal;
    @ApiModelProperty("已用容量")
    private String diskUser;
    @ApiModelProperty("剩余容量")
    private String diskFree;
    @ApiModelProperty("利用率")
    private String diskUserRate;
    @ApiModelProperty("更新时间")
    private String updateTime;
}
