package cn.mw.monitor.report.dto;

import cn.mwpaas.common.utils.DateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 自定义指标报表DTO
 * @date 2023/10/13 15:13
 */
@Data
@ApiModel("报表缓存DTO")
public class MwCustomReportDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("资产IP")
    private String assetIp;

    @ApiModelProperty("资产主键ID")
    private String assetsId;

    @ApiModelProperty("资产服务器ID")
    private Integer serverId;

    @ApiModelProperty("资产主机ID")
    private String hostId;

    @ApiModelProperty("业务系统")
    private String businessSystem;

    @ApiModelProperty("单位")
    private String units;

    @ApiModelProperty("平均值")
    private String avgValue;

    @ApiModelProperty("最大值")
    private String maxValue;

    @ApiModelProperty("最小值")
    private String minValue;

    @ApiModelProperty("时间")
    private String date;

    @ApiModelProperty("指标信息")
    private List<MwCustomReportIndexDto> reportIndexDtos;


    public void extractFrom(MwReportTrendCacheDto reportTrendCacheDto){
        this.id = reportTrendCacheDto.getId();
        this.assetsName = reportTrendCacheDto.getAssetsName();
        this.assetIp = reportTrendCacheDto.getAssetIp();
        this.assetsId = reportTrendCacheDto.getAssetsId();
        this.serverId = reportTrendCacheDto.getServerId();
        this.hostId = reportTrendCacheDto.getHostId();
        this.units = reportTrendCacheDto.getUnits();
        this.avgValue = reportTrendCacheDto.getAvgValue();
        this.maxValue = reportTrendCacheDto.getMaxValue();
        this.minValue = reportTrendCacheDto.getMinValue();
        this.date = DateUtils.formatDateTime(reportTrendCacheDto.getDate());
    }

}
