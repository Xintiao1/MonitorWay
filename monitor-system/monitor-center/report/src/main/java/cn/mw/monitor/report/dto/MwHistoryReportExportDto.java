package cn.mw.monitor.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 历史报表导出DTO
 * @date 2023/11/1 14:21
 */
@Data
@ApiModel("报表缓存DTO")
public class MwHistoryReportExportDto {

    @ExcelProperty(value = {"资产名称"})
    private String assetsName;

    @ExcelProperty(value = {"资产IP"})
    private String assetIp;

    @ExcelProperty(value = {"业务系统"})
    private String businessSystem;

    @ExcelProperty(value = {"时间"})
    private String date;

    @ExcelProperty(value = {"指标名称"})
    private String itemChnName;

    @ExcelProperty(value = {"分区名称"})
    private String partitionName;

    @ExcelProperty(value = {"平均值"})
    private String avgValue;

    @ExcelProperty(value = {"最大值"})
    private String maxValue;

    @ExcelProperty(value = {"最小值"})
    private String minValue;

    @ExcelProperty(value = {"单位"})
    private String units;

    public void extractFrom(MwCustomReportDto customReportDto,MwCustomReportIndexDto indexDto){
       this.assetsName = customReportDto.getAssetsName();
       this.assetIp = customReportDto.getAssetIp();
       this.businessSystem = customReportDto.getBusinessSystem();
       this.date = customReportDto.getDate();
       this.itemChnName = indexDto.getItemChnName();
       this.partitionName = indexDto.getPartitionName();
       this.avgValue = customReportDto.getAvgValue();
       this.maxValue = customReportDto.getMaxValue();
       this.minValue = customReportDto.getMinValue();
       this.units = customReportDto.getUnits();
    }
}
