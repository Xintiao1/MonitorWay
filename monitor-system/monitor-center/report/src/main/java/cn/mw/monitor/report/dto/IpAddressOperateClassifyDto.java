package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName IpAddressOperateClassifyDto
 * @Description IPguan里操作分类统计
 * @Author gengjb
 * @Date 2023/3/7 9:55
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAddressOperateClassifyDto {

    @ApiModelProperty("操作名称")
    private String operateName;

    @ApiModelProperty("分配操作次数")
    private Integer disOperateNumber;

    @ApiModelProperty("变更操作次数")
    private Integer updateOperateNumber;

    @ApiModelProperty("回收操作次数")
    private Integer retrieveOperateNumber;
}
