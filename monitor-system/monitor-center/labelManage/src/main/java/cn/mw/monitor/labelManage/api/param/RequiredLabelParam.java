package cn.mw.monitor.labelManage.api.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/1/5 14:15
 *
 *  // 资产类型
 *     private List<Integer> assetsTypeIdList;
 *     // 模块类型
 *     private List<Integer> modeList;
 */
@Data
public class RequiredLabelParam {
    @ApiModelProperty("资产类型id")
    private Integer assetsTypeId;
    @ApiModelProperty("模块id")
    private Integer  moduleId;
}
