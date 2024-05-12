package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName PatrolInspectionDeviceDto
 * @Description 区域设备状态
 * @Author gengjb
 * @Date 2022/10/26 15:00
 * @Version 1.0
 **/
@Data
public class PatrolInspectionDeviceDto {

    @ApiModelProperty("检查内容")
    private String inspectionContent;

    @ApiModelProperty("检查结果")
    private String inspectionResult;

    @ApiModelProperty("说明")
    private String message;
}
