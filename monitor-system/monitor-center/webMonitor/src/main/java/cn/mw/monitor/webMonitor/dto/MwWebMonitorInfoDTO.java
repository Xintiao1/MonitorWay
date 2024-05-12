package cn.mw.monitor.webMonitor.dto;

import cn.mw.monitor.service.server.api.dto.ItemApplication;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/25
 */
@Data
@ApiModel("web监控信息DTO")
public class MwWebMonitorInfoDTO {

    @ApiModelProperty("最后跟新时间")
    private Date lastUpdateTime;

    @ApiModelProperty("历史记录列表")
    private List<MwHistoryDTO> dataList;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("监控标题")
    private String titleName;

    @ApiModelProperty("最后更新时间")
    private String lastUpdateValue;

    @ApiModelProperty("延迟")
    private String delay;

    @ApiModelProperty("监控项信息")
    private List<ItemApplication> itemsList;
}
