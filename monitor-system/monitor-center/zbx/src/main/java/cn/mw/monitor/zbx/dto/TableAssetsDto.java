package cn.mw.monitor.zbx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/5/8 12:05
 */
@Data
public class TableAssetsDto {
    @ApiModelProperty("资产状态")
    private String enable;
    @ApiModelProperty("是否启用监控")
    private Integer monitorFlag;
    @ApiModelProperty("资产名称")
    private String assetsName;
    @ApiModelProperty("主机名称")
    private String hostName;
    @ApiModelProperty("厂商")
    private String manufacturer;
    @ApiModelProperty("系统类型")
    private String assetsTypeName;
    @ApiModelProperty("规格型号")
    private String specifications;
    @ApiModelProperty("描述")
    private String description;
}
