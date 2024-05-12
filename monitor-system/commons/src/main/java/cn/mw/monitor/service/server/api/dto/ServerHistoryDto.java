package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/4/28 11:39
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerHistoryDto extends ServerDTO {
    @ApiModelProperty("时间类型，1：hour 2:day 3:week 4:month 5:自定义")
    private Integer dateType;
    @ApiModelProperty("当时间类型为 5:自定义时,开始时间")
    private String  dateStart;
    @ApiModelProperty("当时间类型为 5:自定义时,结束时间")
    private String dateEnd;

    @ApiModelProperty("数据类型：平均值（AVG）最大值（MAX）最小值（MIN）")
    private String valueType;

    private List<ItemApplication> itemApplicationList;

    private String startTime;
    private String endTime;

    private String webName;
    @ApiModelProperty("监控类型,1:下载速度,2:响应时间")
    private String titleType;

    private String lineName;

    @ApiModelProperty("是否取趋势数据")
    private Boolean isTrend;

    @ApiModelProperty("资产状态")
    private String assetsStatus;
}
