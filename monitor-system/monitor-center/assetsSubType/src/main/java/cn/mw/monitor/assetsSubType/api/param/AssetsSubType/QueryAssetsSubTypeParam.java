package cn.mw.monitor.assetsSubType.api.param.AssetsSubType;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/4/1
 */
@Data
@ApiModel("查询资产分类实体类")
public class QueryAssetsSubTypeParam extends BaseParam {
    @ApiModelProperty(name = "父分类ID" )
    private Integer pid;

    @ApiModelProperty(name = "父分类ID" )
    private Integer labelId;

    private Integer classify;
}
