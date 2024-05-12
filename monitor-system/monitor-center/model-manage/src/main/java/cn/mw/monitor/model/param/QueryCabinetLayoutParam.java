package cn.mw.monitor.model.param;

import cn.mw.monitor.service.model.param.CabinetLayoutDataParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @date 2022/4/24
 */
@Data
@ApiModel
public class QueryCabinetLayoutParam {
    @ApiModelProperty("上次保存的机柜布局数据")
    private CabinetLayoutDataParam lastData;
    @ApiModelProperty("本次保存的机柜布局数据")
    private CabinetLayoutDataParam currentData;
    @ApiModelProperty("模型实例id")
    private Integer instanceId;
    @ApiModelProperty("模型id")
    private Integer modelId;
    @ApiModelProperty("模型Index")
    private String modelIndex;
    //当前实例Id(刀片服务器删除使用)
    private String currentInstanceId;
    //所属刀箱Id
    private String chassisInstanceId;
}
