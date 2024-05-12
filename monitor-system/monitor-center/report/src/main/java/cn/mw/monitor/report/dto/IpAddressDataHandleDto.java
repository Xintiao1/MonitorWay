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
 * @Date 2023/3/14 16:06
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAddressDataHandleDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("网段IP利用率TopN")
    private List<IpAddressUtilizationTopNDto> utilizationTopNDtos;

    @ApiModelProperty("IP管理操作分类统计")
    private IpAddressOperateClassifyDto ipAddressOperateClassifyDto;

    @ApiModelProperty("分配/变更次数TopN")
    private List<IpAddressUpdateNumberDto> ipAddressUpdateNumberDtos;
}
