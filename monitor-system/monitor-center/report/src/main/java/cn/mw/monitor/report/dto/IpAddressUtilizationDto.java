package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName
 * @Description IP地址使用率统计
 * @Author gengjb
 * @Date 2023/3/7 9:34
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAddressUtilizationDto {

    @ApiModelProperty("IP分组名称")
    private String groupName;

    @ApiModelProperty("地址段总数")
    private Integer ipAddressSegmentAmount = 0;

    @ApiModelProperty("小于等于50%数量")
    private Integer ltEqualToFiftyAmount = 0;

    @ApiModelProperty("50%到80%数量")
    private Integer fiftyToEightyAmount = 0;

    @ApiModelProperty("查询下级分组id")
    private Integer id= 0;

    @ApiModelProperty("大于等于80%数量")
    private Integer gtEqualToEightyAmount = 0;
}
