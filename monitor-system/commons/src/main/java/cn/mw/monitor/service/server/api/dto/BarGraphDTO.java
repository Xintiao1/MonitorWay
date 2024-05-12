package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/4/28 15:14
 * @Version 1.0
 */
@Data
public class BarGraphDTO {
    //    监控项名称
    @ApiModelProperty("监控项名称")
    private List<String> itemNames;

    @ApiModelProperty("组件对应的基础信息，必填")
    private AssetsBaseDTO assetsBaseDTO;

    @ApiModelProperty("所选监控项的对应信息")
    private List<ItemApplication> itemApplicationList;

    @ApiModelProperty("折线图中下拉框的所选字段")
    private String typeItemName;
}
