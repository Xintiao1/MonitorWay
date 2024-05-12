package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mwpaas.common.utils.DateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author gengjb
 * @description 容器大屏告警DTO
 * @date 2023/9/18 15:44
 */
@Data
@ApiModel("接口信息DTO")
public class MwVisualizedContainerDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("告警等级")
    private String alertLevel;

    @ApiModelProperty("告警数量")
    private Integer alertCount;

    @ApiModelProperty("告警时间")
    private String alertDate;

    @ApiModelProperty("分区名称")
    private String partitionName;

    public void extractFrom(MwVisualizedPrometheusDropDto prometheusDropDto){
        this.alertLevel = prometheusDropDto.getDesc();
        this.partitionName = prometheusDropDto.getPartitionName();
        this.alertDate = DateUtils.formatDate(new Date());
    }
}
