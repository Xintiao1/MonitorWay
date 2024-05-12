package cn.mw.monitor.report.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 磁盘信息DTO
 * @date 2024/1/29 9:42
 */
@Data
@ApiModel("磁盘信息DTO")
public class MwReportDiskDto {

    @ApiModelProperty("资产ID")
    private String assetsId;

    @ApiModelProperty("资产名称")
    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;

    @ApiModelProperty("资产IP")
    @ExcelProperty(value = {"资产IP"},index = 1)
    private String assetsIp;

    @ApiModelProperty("磁盘名称")
    @ExcelProperty(value = {"磁盘名称"},index = 2)
    private String diskName;

    @ApiModelProperty("磁盘总量")
    @ExcelProperty(value = {"磁盘总量"},index = 3)
    private String diskTotal;

    @ApiModelProperty("磁盘已使用")
    @ExcelProperty(value = {"磁盘已使用"},index = 4)
    private String diskUse;

    @ApiModelProperty("磁盘利用率")
    @ExcelProperty(value = {"磁盘利用率"},index = 5)
    private String diskUtilization;


    public void extractFrom(ItemApplication itemApplication, MwTangibleassetsTable tangibleassetsTable){
        this.assetsId = tangibleassetsTable.getId();
        this.assetsName = tangibleassetsTable.getAssetsName();
        this.assetsIp = tangibleassetsTable.getInBandIp();
        this.diskName = itemApplication.getChName();
    }
}
