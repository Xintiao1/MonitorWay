package cn.mw.monitor.report.dto.linkdto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/12/25 11:19
 */
@Data
public class InterfaceReportDtos {
    @ApiModelProperty("线路id")
    private String interfaceID;
    @ApiModelProperty(value = "线路名称")
    private String caption;
    @ApiModelProperty(value = "上行带宽")
    private Double inBandwidth;
    @ApiModelProperty(value = "下行带宽")
    private Double outBandwidth;
    @ApiModelProperty(value = "带宽单位")
    private String bandUnit;
    @ApiModelProperty(value = "接入流量(入向)平均")
    private Double inAveragebps;
    @ApiModelProperty(value = "接入流量(入向)最小")
    private Double inMinbps;
    @ApiModelProperty(value = "接入流量(入向)最大")
    private Double inMaxbps;
    @ApiModelProperty(value = "接出流量(出向)最小")
    private Double outMinbps;
    @ApiModelProperty(value = "接出流量(出向)平均")
    private Double outAveragebps;
    @ApiModelProperty(value = "接出流量(出向)最大")
    private Double outMaxbps;
    @ApiModelProperty(value = "接入流量(入向)平均利用率")
    private Double inAvgUse;
    @ApiModelProperty(value = "接出流量(出向)平均利用率")
    private Double outAvgUse;
    @ApiModelProperty(value = "接入流量(入向)最大利用率")
    private Double inMaxUse;
    @ApiModelProperty(value = "接出流量(出向)最大利用率")
    private Double outMaxUse;
    @ApiModelProperty(value = "接口流量时间占比(入向)<10%")
    private Double inProportionTen;
    @ApiModelProperty(value = "接口流量时间占比(入向)10%-50%")
    private Double inProportionFifty;
    @ApiModelProperty(value = "接口流量时间占比(入向)50%-80%")
    private Double inProportionEighty;
    @ApiModelProperty(value = "接口流量时间占比(入向)>80%")
    private Double inProportionHundred;
    @ApiModelProperty(value = "接口流量时间占比(出向)<10%")
    private Double outProportionTen;
    @ApiModelProperty(value = "接口流量时间占比(出向)10%-50%")
    private Double outProportionFifty;
    @ApiModelProperty(value = "接口流量时间占比(出向)50%-80%")
    private Double outProportionEighty;
    @ApiModelProperty(value = "接口流量时间占比(出向)>80%")
    private Double outProportionHundred;

    @ApiModelProperty(value = "服务器ID")
    private Integer serverId;

    @ApiModelProperty(value = "主机ID")
    private String hostId;

    @ApiModelProperty(value = "接口名称")
    private String interfaceName;

    private Date dateTime;

    public void extractFrom(Integer serverId,String hostId,String interfaceName){
        this.serverId = serverId;
        this.hostId = hostId;
        this.interfaceName = interfaceName;
    }

}
