package cn.mw.monitor.server.param;

import cn.mw.monitor.service.server.api.dto.AssetsBaseDTO;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/2/3 10:05
 * @Version 1.0
 */
@Data
public class QueryArgumentsParam {
    @ApiModelProperty("是否带名称下拉")
    private boolean dropdownFlag;
    @ApiModelProperty("是否需要百分比数据")
    private boolean percentFlag;
    @ApiModelProperty("是否监控项多选")
    private boolean multipleFlag;
    @ApiModelProperty("是否监控项单位不做控制")
    private boolean withoutUnitsFlag;
    @ApiModelProperty("是否为数字数据")
    private boolean figureFlag;
    @ApiModelProperty("基础资产数据，必填项")
    private AssetsBaseDTO assetsBaseDTO;
    @ApiModelProperty("所选第一个监控项的对应信息")
    private ItemApplication itemApplication;
}
