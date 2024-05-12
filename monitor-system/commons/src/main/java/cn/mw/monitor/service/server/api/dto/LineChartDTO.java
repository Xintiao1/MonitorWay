package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/2/5 9:54
 * @Version 1.0
 */
@Data
public class LineChartDTO {
    @ApiModelProperty("时间类型，1：hour 2:day 3:week 4:month 5:自定义")
    private Integer dateType = 1;
    @ApiModelProperty("当时间类型为 5:自定义时,开始时间")
    private String dateStart;
    @ApiModelProperty("当时间类型为 5:自定义时,结束时间")
    private String dateEnd;

    @ApiModelProperty("数据类型：平均值（AVG）最大值（MAX）最小值（MIN）")
    private String valueType = "AVG";
    //    监控项名称
    @ApiModelProperty("监控项名称")
    private List<String> itemNames;

    @ApiModelProperty("组件对应的基础信息，必填")
    private AssetsBaseDTO assetsBaseDTO;

    @ApiModelProperty("所选监控项的对应信息")
    private List<ItemApplication> itemApplicationList;

    @ApiModelProperty("折线图中下拉框的所选字段")
    private String typeItemName;


    private boolean noSelectChart;

    @ApiModelProperty("单个折线图")
    private boolean isSingleLine;


    private List<String> typeItemNames;

    @ApiModelProperty("是否趋势数据")
    private Boolean isTrend;

    @ApiModelProperty("主机名称")
    private String hostName;

    @ApiModelProperty("资产状态")
    private String assetsStatus;
}
