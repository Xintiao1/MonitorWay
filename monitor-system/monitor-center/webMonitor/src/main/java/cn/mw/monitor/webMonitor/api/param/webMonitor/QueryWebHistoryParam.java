package cn.mw.monitor.webMonitor.api.param.webMonitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Tolerate;

import java.util.Date;

/**
 * @author baochengbin
 * @date 2020/4/26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryWebHistoryParam {
    private Integer monitorServerId;

    private String webName;
    @ApiModelProperty("时间类型，1：hour 2:day 3:week 4:month")
    private Integer dateType;

    private String itemName;

    private String key;
    //资产主键id
    private String assetsId;

    private String tangibleId;

    private Date dateStart;

    private Date dateEnd;

    @ApiModelProperty("监控类型,1:下载速度,2:响应时间")
    private Integer titleType;





}
