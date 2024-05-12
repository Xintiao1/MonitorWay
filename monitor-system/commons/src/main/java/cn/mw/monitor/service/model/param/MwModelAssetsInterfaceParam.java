package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/3/8 12:11
 */
@Data
@ApiModel
public class MwModelAssetsInterfaceParam {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("资产id")
    private String deviceId;
    @ApiModelProperty("接口名称")
    private String interfaceName;
    @ApiModelProperty("接口描述")
    private String interfaceDescr;
    @ApiModelProperty("接口显示设置")
    private Boolean showFlag;
    @ApiModelProperty("告警设置")
    private Boolean alertTag;

    private List<String> ids;
}
