package cn.mw.monitor.report.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 自定义指标报表参数
 * @date 2023/10/12 16:24
 */
@Data
@ApiModel("自定义指标报表参数")
public class MwCustomReportParam extends BaseParam {

    @ApiModelProperty("指标信息")
    private List<String> indexs;

    @ApiModelProperty("资产信息")
    private List<String> assetsIds;

    @ApiModelProperty("查询时间类型，1：昨天，5：上周，8：上月，12自定义")
    private Integer dateType;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("是否查询最新数据,true:最新数据，false:历史数据")
    private Boolean isLatestData;

    @ApiModelProperty("表头信息")
    private List<String> tableHeads;

}
