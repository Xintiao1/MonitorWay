package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 报表安全门限Dto
 * @Author gengjb
 * @Date 2023/6/9 10:49
 * @Version 1.0
 **/
@Data
public class MwReportSafeValueDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("CPU安全门限")
    private Integer cpuSafeValue;

    @ApiModelProperty("内存安全门限")
    private Integer memorySafeValue;

    @ApiModelProperty("接口安全门限")
    private Integer interfaceSafeValue;

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("接口描述")
    private String interFaceDesc;

}
