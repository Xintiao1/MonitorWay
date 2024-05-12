package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * @ClassName PatrolInspectionRunStatusDto
 * @Description 巡检报告运行状态数据实体
 * @Author gengjb
 * @Date 2022/10/26 14:49
 * @Version 1.0
 **/
@Data
public class PatrolInspectionRunStatusDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("资产总数量")
    private Integer assetsTotal;

    @ApiModelProperty("正常资产数量")
    private Integer normalAssetsCount;

    @ApiModelProperty("异常资产数量")
    private Integer abnormalAssetsCount;
}
