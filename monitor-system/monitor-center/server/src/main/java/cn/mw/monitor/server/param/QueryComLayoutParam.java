package cn.mw.monitor.server.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/2/24 16:03
 * @Version 1.0
 */
@Data
public class QueryComLayoutParam {
    @ApiModelProperty("用户id")
    private Integer userId;
//    @ApiModelProperty("资产子类型id")
//    private Integer assetsTypeSubId;
    @ApiModelProperty("监控服务器id")
    private Integer monitorServerId;
    @ApiModelProperty("模板id")
    private String templateId;
    @ApiModelProperty("标签id")
    private int navigationBarId;
    @ApiModelProperty("资产id")
    private int assetsId;
}
