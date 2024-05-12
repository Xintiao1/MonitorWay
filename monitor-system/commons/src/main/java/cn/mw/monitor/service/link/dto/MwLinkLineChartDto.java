package cn.mw.monitor.service.link.dto;

import cn.mw.monitor.service.server.api.dto.MWItemHistoryDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName MwLinkLineChartDto
 * @Description 线路折线图数据实体
 * @Author gengjb
 * @Date 2023/2/15 10:47
 * @Version 1.0
 **/
@Data
public class MwLinkLineChartDto {

    @ApiModelProperty("最后修改时间")
    private Date lastUpdateTime;

    @ApiModelProperty("返回数据")
    private List<MWItemHistoryDto> realData;

    @ApiModelProperty("返回最大值数据")
    private List<MWItemHistoryDto> maxData;

    @ApiModelProperty("返回最小值数据")
    private List<MWItemHistoryDto> minData;

    @ApiModelProperty("返回平均值数据")
    private List<MWItemHistoryDto> avgData;

    @ApiModelProperty("返回单位")
    private String unitByReal;

    @ApiModelProperty("返回平均值单位")
    private String unitByAvg;

    @ApiModelProperty("返回最大值单位")
    private String unitByMax;

    @ApiModelProperty("返回最小值单位")
    private String unitByMin;

    @ApiModelProperty("显示名称")
    private String titleName;

}
