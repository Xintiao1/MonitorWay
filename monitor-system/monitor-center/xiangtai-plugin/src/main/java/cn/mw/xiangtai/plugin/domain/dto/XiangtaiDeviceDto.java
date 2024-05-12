package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 设备信息DTO
 * @date 2023/10/27 11:07
 */
@ApiModel("设备信息DTO")
@Data
public class XiangtaiDeviceDto {

    @ApiModelProperty("IP地址段")
    private String ipAddressSegment;

    @ApiModelProperty("地址信息")
    private String addressInfo;

    @ApiModelProperty("IP地址段对应区域")
    private String ipArea;

}
