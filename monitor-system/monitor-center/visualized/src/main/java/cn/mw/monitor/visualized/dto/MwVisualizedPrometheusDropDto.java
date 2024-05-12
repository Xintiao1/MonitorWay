package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description 可视化Prometheus监控指标下拉Dto
 * @Author gengjb
 * @Date 2023/6/7 14:52
 * @Version 1.0
 **/
@Data
@ApiModel("可视化Prometheus监控指标下拉Dto")
public class MwVisualizedPrometheusDropDto {

    @ApiModelProperty("主键")
    private String id;

    @ApiModelProperty("描述")
    private String desc;

    @ApiModelProperty("请求地址")
    private String url;

    @ApiModelProperty("分区名称")
    private String partitionName;

    @ApiModelProperty("参数")
    private String param;

    @ApiModelProperty("值单位")
    private String units;

    @ApiModelProperty("监控项")
    private String itemName;
}
