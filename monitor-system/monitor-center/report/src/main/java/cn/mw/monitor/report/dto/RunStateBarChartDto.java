package cn.mw.monitor.report.dto;

import cn.mw.monitor.report.dto.assetsdto.RunTimeItemValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 运行状态图DTO
 * @date 2023/9/15 15:04
 */
@Data
@ApiModel("运行状态图DTO")
public class RunStateBarChartDto {

    @ApiModelProperty("值")
    private Double value;

    @ApiModelProperty("资产名称")
    private String assetsName;

    @ApiModelProperty("监控项名称")
    private String itemName;

    @ApiModelProperty("类型名称")
    private String assetsTypeName;

    public void extractFrom(RunTimeItemValue itemValue, String assetsTypeName){
        this.value = Double.parseDouble(itemValue.getAvgValue());
        this.assetsName = itemValue.getAssetName();
        this.itemName = itemValue.getItemName();
        this.assetsTypeName = assetsTypeName;
    }
}
