package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 报表指标DTO
 * @date 2023/9/25 11:22
 */
@Data
@ApiModel("报表指标DTO")
public class MwReportIndexDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("监控项名称")
    private String itemName;

    @ApiModelProperty("中文名称")
    private String chnName;
}
