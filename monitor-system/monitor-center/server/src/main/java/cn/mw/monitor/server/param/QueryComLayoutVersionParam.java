package cn.mw.monitor.server.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author qzg
 * @Date 2022/3/16
 */
@Data
public class QueryComLayoutVersionParam {
    @ApiModelProperty("布局id")
    private Integer comLayoutId;
    @ApiModelProperty("回撤步数")
    private Integer retreatNum;
}
