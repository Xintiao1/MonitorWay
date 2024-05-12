package cn.mw.monitor.link.dto;

import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/7/22 9:40
 */
@Data
public class NetWorkLinkDto extends AddAndUpdateParam {
    /**
     * 从zabbix端获取的数据
     */
    @ApiModelProperty("监控状态")
    private String status;
    @ApiModelProperty("链路响应时间")
    private String responseTime;
    @ApiModelProperty("链路丢包率")
    private String lossRate;

    @ApiModelProperty("IN带宽利用率")
    private Double inLinkBandwidthUtilization = 0D;
    @ApiModelProperty("OUT带宽利用率")
    private Double outLinkBandwidthUtilization = 0D;




    private String baseLinkHostId;
    private Integer baseLinkServerId;
    private String basePort;
    private String enableLinkHostId;
    private Integer enableLinkServerId;


    private Double nqaSent;

    private Double nqaSucess;


    private Integer dateType;

    private String dateStart;

    private String dateEnd;

    private int trendType;
}
