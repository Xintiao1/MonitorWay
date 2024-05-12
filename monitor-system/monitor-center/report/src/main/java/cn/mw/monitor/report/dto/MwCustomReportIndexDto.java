package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 指标信息DTO
 * @date 2023/10/13 15:15
 */
@Data
@ApiModel("指标信息DTO")
public class MwCustomReportIndexDto {

    @ApiModelProperty("监控项名称")
    private String itemName;

    @ApiModelProperty("监控项中文名称")
    private String itemChnName;

    @ApiModelProperty("值")
    private String value;

    @ApiModelProperty("单位")
    private String units;

    @ApiModelProperty("分区名称")
    private String partitionName;


    public void extractFrom(MwReportTrendCacheDto reportTrendCacheDto,String itemChnName){
        this.itemName = reportTrendCacheDto.getItemName();
        this.itemChnName = itemChnName;
        this.value = reportTrendCacheDto.getLastValue();
        this.units = reportTrendCacheDto.getUnits();
        this.partitionName = reportTrendCacheDto.getPartitionName();
    }

}
