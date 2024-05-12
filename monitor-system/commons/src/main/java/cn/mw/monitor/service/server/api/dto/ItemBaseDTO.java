package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/3/12 16:12
 * @Version 1.0
 */
@Data
public class ItemBaseDTO {
    @ApiModelProperty("监控项名称")
    private List<String> itemNames;

    @ApiModelProperty("所选监控项的对应信息")
    private List<ItemApplication> itemApplicationList;

    @ApiModelProperty("组件对应的基础信息，必填")
    private AssetsBaseDTO assetsBaseDTO;
}
