package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.server.api.dto.ItemTrendApplication;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName
 * @Description 告警信息记录DTO
 * @Author gengjb
 * @Date 2023/6/6 14:41
 * @Version 1.0
 **/
@Data
@ApiModel("告警信息记录DTO")
public class MwVisualizedAlertRecordDto {

    @ApiModelProperty("主键ID")
    private String cacheId;

    @ApiModelProperty("资产ID")
    private String assetsId;

    @ApiModelProperty("主机ID")
    private String hostId;

    @ApiModelProperty("告警等级")
    private String alertSeverity;

    @ApiModelProperty("记录时间")
    private String time;


    public void extractFrom(ZbxAlertDto alertDto, String time){
       this.assetsId = alertDto.getId();
       this.hostId = alertDto.getHostid();
       this.alertSeverity = alertDto.getSeverity();
       this.time = time;
    }
}
