package cn.mw.xiangtai.plugin.domain.vo;

import cn.mw.xiangtai.plugin.domain.dto.LongitudeLatitudeDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("地图信息对象")
public class PositionMapVO {

    @ApiModelProperty("源地址对象")
    private LongitudeLatitudeDTO srcAddressObj;

    @ApiModelProperty("目的地址对象")
    private LongitudeLatitudeDTO dstAddressObj;

    @ApiModelProperty("状态")
    private String status;
}
