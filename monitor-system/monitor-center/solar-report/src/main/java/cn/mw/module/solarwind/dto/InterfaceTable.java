package cn.mw.module.solarwind.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @author xhy
 * @date 2020/6/23 14:29
 */
@Data
public class InterfaceTable {
    @ApiModelProperty("线路id")
    private Integer interfaceID;
    @ApiModelProperty("线路名称")
    private String caption;
    @ApiModelProperty("线路分支名称")
    private String carrierName;
    @ApiModelProperty("带宽")
    private Float inBandwidth;
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
