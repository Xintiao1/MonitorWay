package cn.mw.monitor.service.assets.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class RefreshInterfaceParam {
    @ApiModelProperty("更新方式(All: 全部, Cust:条件更新)")
    private String refreshType;

    @ApiModelProperty("资产id")
    private List<String> assetIds;
}
