package cn.mw.monitor.wireless.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author qzg
 * @Date 2021/6/23
 */
@Data
@ApiModel(value = "SSID端查询信息实体")
public class QueryWirelessSSIDDataShowDTO extends BaseParam {
    @ApiModelProperty("接收流量")
    private String rxBytesInfo;
    @ApiModelProperty("发送流量")
    private String txBytesInfo;
    @ApiModelProperty("用户数")
    private String userNum;
    @ApiModelProperty("SSID名称")
    private String name;
    @ApiModelProperty("所属资产名称")
    private String assetName;

    private Double sortRxBytesInfo;
    private Double sortTxBytesInfo;
    private Integer sortUserNum;

}
