package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName
 * @Description IP报表返回数据结构
 * @Author gengjb
 * @Date 2023/3/13 19:35
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAddressReportDto {

    @ApiModelProperty("IP地址使用率统计数据")
    private List<IpAddressUtilizationDto> ipAddressSegmentDto;

    @ApiModelProperty("网段IP利用率TopN数据")
    private Map<String, List<IpAddressUtilizationTopNDto>> utilizationTopNMap;

    @ApiModelProperty("IP管理操作分类统计数据")
    private  Map<String, IpAddressOperateClassifyDto> classifyDtoMap;

    @ApiModelProperty("分配/变更次数TopN数据")
    private Map<String, List<IpAddressUpdateNumberDto>> updateNumberMap;
}
