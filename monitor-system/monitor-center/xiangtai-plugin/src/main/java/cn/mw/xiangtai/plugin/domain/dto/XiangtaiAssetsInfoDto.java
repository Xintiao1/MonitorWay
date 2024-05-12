package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 祥泰资产信息DTO
 * @date 2023/10/25 8:59
 */
@ApiModel("祥泰资产信息DTO")
@Data
public class XiangtaiAssetsInfoDto {

    @ApiModelProperty("在线设备")
    private Integer onlineDevice;

    @ApiModelProperty("总资产数")
    private Integer totalAssetsNumber;

    @ApiModelProperty("漏洞数量")
    private Integer bugNumber;
}
