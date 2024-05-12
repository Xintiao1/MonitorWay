package cn.mw.monitor.report.dto.linkdto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2525/7/1 19:13
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterfaceReportDto {
    @ExcelIgnore
    private String interfaceID;
    @ExcelProperty(value = "线路名称")
    private String caption;
    @ColumnWidth(15)
    @ExcelProperty(value = "上行带宽")
    private String inBandwidth;
    @ExcelProperty(value = "下行带宽")
    private String outBandwidth;
    @ColumnWidth(15)
    @ExcelProperty(value = "接入流量(入向)平均")
    private String inAveragebps;
    @ColumnWidth(15)
    @ExcelProperty(value = "接入流量(入向)最小")
    private String inMinbps;
    @ExcelProperty(value = "接入流量(入向)最大")
    @ColumnWidth(15)
    private String inMaxbps;
    @ExcelProperty(value = "接出流量(出向)最小")
    @ColumnWidth(15)
    private String outMinbps;
    @ExcelProperty(value = "接出流量(出向)平均")
    @ColumnWidth(15)
    private String outAveragebps;
    @ExcelProperty(value = "接出流量(出向)最大")
    @ColumnWidth(15)
    private String outMaxbps;
    @ExcelProperty(value = "接入流量(入向)平均利用率")
    @ColumnWidth(15)
    private String inAvgUse;
    @ExcelProperty(value = "接出流量(出向)平均利用率")
    @ColumnWidth(15)
    private String outAvgUse;
    @ExcelProperty(value = "接入流量(入向)最大利用率")
    @ColumnWidth(15)
    private String inMaxUse;
    @ExcelProperty(value = "接出流量(出向)最大利用率")
    @ColumnWidth(15)
    private String outMaxUse;
    @ExcelProperty(value = "接口流量时间占比(入向)<10%")
    @ColumnWidth(15)
    private String inProportionTen;
    @ExcelProperty(value = "接口流量时间占比(入向)10%-50%")
    @ColumnWidth(18)
    private String inProportionFifty;
    @ExcelProperty(value = "接口流量时间占比(入向)50%-80%")
    @ColumnWidth(18)
    private String inProportionEighty;
    @ExcelProperty(value = "接口流量时间占比(入向)>80%")
    @ColumnWidth(15)
    private String inProportionHundred;
    @ExcelProperty(value = "接口流量时间占比(出向)<10%")
    @ColumnWidth(15)
    private String outProportionTen;
    @ExcelProperty(value = "接口流量时间占比(出向)10%-50%")
    @ColumnWidth(18)
    private String outProportionFifty;
    @ExcelProperty(value = "接口流量时间占比(出向)50%-80%")
    @ColumnWidth(18)
    private String outProportionEighty;
    @ExcelProperty(value = "接口流量时间占比(出向)>80%")
    @ColumnWidth(15)
    private String outProportionHundred;

    @ApiModelProperty(value = "服务器ID")
    private Integer serverId;

    @ApiModelProperty(value = "主机ID")
    private String hostId;

    @ApiModelProperty(value = "接口名称")
    private String interfaceName;

    public void extractFrom(Integer serverId,String hostId,String interfaceName){
        this.serverId = serverId;
        this.hostId = hostId;
        this.interfaceName = interfaceName;
    }
}
