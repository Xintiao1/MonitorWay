package cn.mw.monitor.assets.api.param.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lstart
 * @date 2021/6/7 - 10:22
 */
@ApiModel("IOT子类型数据")
@Data
public class IotTypeParam {

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("此类型资产数量")
    private String assetsCount;

    @ApiModelProperty("类型id")
    private String typeId;

    @ApiModelProperty("父id")
    private String pid;

}
