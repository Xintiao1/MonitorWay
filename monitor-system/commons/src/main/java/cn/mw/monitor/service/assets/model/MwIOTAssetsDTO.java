package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author dev
 * @date 2020/6/15
 */
@ApiModel(value = "iot信息表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwIOTAssetsDTO {
    @ApiModelProperty(value = "地址码")
    private String addressCode;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "资产ID")
    private String assetsId;
}
