package cn.mw.monitor.server.param;

import cn.mw.monitor.service.server.api.dto.AssetsBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 布局设置 高级表格
 *
 * @author qzg
 * @Date 2021/6/30
 * @Version 1.0
 */
@Data
public class QueryAdvanceTableParam {
    @ApiModelProperty("用户id")
    private Integer userId;
    @ApiModelProperty("监控服务器id")
    private Integer monitorServerId;
    @ApiModelProperty("applicationName")
    private String applicationName;
    @ApiModelProperty("hostid")
    private String hostid;
    @ApiModelProperty("资产关联模板id")
    private String templateId;
    @ApiModelProperty("组件对应的基础信息，必填")
    private AssetsBaseDTO assetsBaseDTO;
}
