package cn.mw.monitor.report.dto;

import cn.mw.monitor.report.service.manager.RunTimeReportManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName
 * @Description 内存信息DTO
 * @Author gengjb
 * @Date 2023/6/13 9:43
 * @Version 1.0
 **/
@Data
public class MwReportMemoryDto {

    @ApiModelProperty("服务器ID")
    private Integer serverId;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("监控项")
    private String itemName;

    @ApiModelProperty("总内存")
    private double memoryTotal;

    @ApiModelProperty("已用内存")
    private double memoryUsed;

    @ApiModelProperty("剩余内存")
    private double memoryFree;

    @ApiModelProperty("单位")
    private String units;

    public void extractFrom(double lastValue, String itemName,String units,Integer monitorServerId,String hostid){
        this.serverId = monitorServerId;
        this.hostId = hostid;
        this.units = units;
        this.itemName = itemName;
        if(itemName.contains(RunTimeReportManager.MEMORY_TOTAL)){
            this.memoryTotal = lastValue;
        }
        if(itemName.contains(RunTimeReportManager.MEMORY_USED)){
            this.memoryUsed = lastValue;
        }
        if(itemName.contains(RunTimeReportManager.MEMORY_FREE)){
            this.memoryFree = lastValue;
        }
    }
}
