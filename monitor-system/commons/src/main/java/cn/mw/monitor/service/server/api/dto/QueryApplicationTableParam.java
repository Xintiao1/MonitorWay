package cn.mw.monitor.service.server.api.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/6/21 11:04
 * @Version 1.0
 */
@Data
public class QueryApplicationTableParam extends BaseParam {
    @ApiModelProperty("表格类型对应的应用集名称")
    private List<String> applicationNames;

    @ApiModelProperty("是否分页")
    private boolean limitFlag;

    @ApiModelProperty("监控项名称获取")
    private String typeName;

    @ApiModelProperty("主机id")
    private String assetsId;

    @ApiModelProperty("第三方监控服务器id")
    private int monitorServerId;

    @ApiModelProperty("资产关联带外ip")
    private String outBandIp;

    private boolean hardwareFlag;
}
