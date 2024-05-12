package cn.mw.monitor.assets.api.param.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lstart
 * @date 2021/6/7 - 10:22
 */
@ApiModel("IOT数据")
@Data
public class IotParam {

    @ApiModelProperty("类型id")
    private String typeId;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("此类型资产数量")
    private String assetsCount;

    @ApiModelProperty("子类型列表")
    private List<IotTypeParam> iotTypeParams;
}
