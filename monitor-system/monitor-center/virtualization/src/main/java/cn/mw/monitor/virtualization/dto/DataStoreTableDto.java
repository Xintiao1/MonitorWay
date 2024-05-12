package cn.mw.monitor.virtualization.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/6/30 12:08
 * @Version 1.0
 */
@Data
public class DataStoreTableDto {
    @ApiModelProperty(value = "存储名称")
    private String storeName;
//    @ApiModelProperty(value = "类型")
//    private String type;
//    private String status;
    @ApiModelProperty(value = "读延迟")
    private String readLatency;
    @ApiModelProperty("读延迟")
    private Double sortReadLatency;

    @ApiModelProperty(value = "写延迟")
    private String writeLatency;
    @ApiModelProperty("写延迟")
    private Double sortWriteLatency;

    @ApiModelProperty(value = "总容量")
    private String totalCapacity;
    @ApiModelProperty("总容量")
    private Double sortTotalCapacity;

    @ApiModelProperty(value = "可用容量")
    private String availableCapacity;
    @ApiModelProperty("可用容量")
    private Double sortAvailableCapacity;

    @ApiModelProperty(value = "存储使用率")
    private Double storeUtilization;

}
