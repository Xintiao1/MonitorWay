package cn.mw.monitor.report.dto.linkdto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * @author xhy
 * @date 2020/6/23 14:29
 */
@Data
public class LinkReportTable {
    @ApiModelProperty("线路id")
    private String interfaceID;
    @ApiModelProperty("线路名称")
    private String caption;
    @ApiModelProperty("上行带宽")
    private Float inBandwidth;
    @ApiModelProperty("下行带宽")
    private Float outBandwidth;
    @ApiModelProperty("带宽单位")
    private String bandUnit;
    @ApiModelProperty("接口流入流量平均")
    private Float inAveragebps;
    @ApiModelProperty("接口流入流量最小")
    private Float inMinbps;
    @ApiModelProperty("接口流入流量最大")
    private Float inMaxbps;
    @ApiModelProperty("接口流出流量最小")
    private Float outMinbps;
    @ApiModelProperty("接口流出流量评价")
    private Float outAveragebps;
    @ApiModelProperty("接口流出流量最大")
    private Float outMaxbps;

    private Float inAvgUse;
    private Float outAvgUse;
    private Float inMaxUse;
    private Float outMaxUse;

    private Date dateTime;

    private String tableName;
    private String[] tableNames;
    private String startTime;
    private String endTime;
    private String startTimeDay;
    private String endTimeDay;
}
