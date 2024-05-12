package cn.mw.monitor.report.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName
 * @Description 分配/变更次数TopN
 * @Author gengjb
 * @Date 2023/3/7 9:58
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAddressUpdateNumberDto {

    @ApiModelProperty("IP地址段")
    private String groupName;

    @ApiModelProperty("变更次数")
    private Integer updateNumber;
}
