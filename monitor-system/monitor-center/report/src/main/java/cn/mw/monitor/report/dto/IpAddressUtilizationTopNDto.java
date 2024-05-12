package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName
 * @Description 网段IP利用率TopN
 * @Author gengjb
 * @Date 2023/3/7 9:49
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAddressUtilizationTopNDto {

    @ApiModelProperty("网段名称")
    private String netWorksName;

    @ApiModelProperty("当前使用率")
    private String currUtilization;

    @ApiModelProperty("排序使用")
    private Double sortValue;
}
