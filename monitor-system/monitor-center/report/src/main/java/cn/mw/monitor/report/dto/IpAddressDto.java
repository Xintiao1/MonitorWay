package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName
 * @Description ToDo
 * @Author gengjb
 * @Date 2023/3/14 16:27
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAddressDto {

    @ApiModelProperty("IP地址使用率统计")
    private List<IpAddressUtilizationDto> utilizationDtos;

    @ApiModelProperty("分组统计数据")
    private List<IpAddressDataHandleDto> addressDataHandleDtos;

}
