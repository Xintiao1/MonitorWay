package cn.mw.monitor.service.assets.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author syt
 * @Date 2020/7/15 11:31
 * @Version 1.0
 */
@ApiModel(value = "VCenter信息表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwVCenterAssetsDTO {
    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "用户名称")
    private String userName;

    @ApiModelProperty(value = "路径")
    private String url;

    @ApiModelProperty(value = "端口号")
    private Integer port;

    @ApiModelProperty(value = "资产ID")
    private String assetsId;
}
