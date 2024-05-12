package cn.mw.module.solarwind.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
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
//@HeadRowHeight(35)
//@ContentRowHeight(15)
public class InterfaceReportDto {
    @ExcelIgnore
    private String _XID;
    @ExcelIgnore
    private Integer interfaceID;
    @ExcelProperty(value = "线路名称")
    @ColumnWidth(30)
    private String caption;
    //@ExcelProperty(value = "线路分支名称")
    @ExcelIgnore
    private String carrierName;
    @ColumnWidth(15)
    @ExcelProperty(value = "带宽")
    private String inBandwidth;
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
}
