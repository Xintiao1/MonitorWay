package cn.mw.xiangtai.plugin.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 祥泰地图区域DTO
 * @date 2023/11/16 21:45
 */
@ApiModel("祥泰地图区域DTO")
@Data
public class XIangtaiMapAreaDto {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("完整名称")
    private String wholeName;

    @ApiModelProperty("经度")
    private String lon;

    @ApiModelProperty("纬度")
    private String lat;
}
