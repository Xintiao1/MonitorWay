package cn.mw.monitor.visualized.param;

import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.visualized.dto.MwVisualizedPrometheusDropDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 组件区参数
 * @Author gengjb
 * @Date 2023/4/17 10:31
 * @Version 1.0
 **/
@Data
@ApiModel("可视化组件区参数")
public class MwVisualizedModuleParam {

    @ApiModelProperty("图类型")
    private Integer chartType;

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("时间类型")
    private Integer dateType;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("模型属性索引")
    private String propertiesIndexId;

    @ApiModelProperty("模型实例id")
    private Integer modelInstanceId;

    @ApiModelProperty("监控项名称")
    private String itemName;

    @ApiModelProperty("监控项名称集合")
    private List<String> itemNames;

    @ApiModelProperty("数据类型,AVG:平均")
    private String dataType;

    @ApiModelProperty("资产ID")
    private String assetsId;

    @ApiModelProperty("资产ID集合")
    private List<String> assetsIds;

    @ApiModelProperty("是否多条折线")
    private Boolean isMultiLine;

    @ApiModelProperty("服务器名称")
    private String serverName;

    @ApiModelProperty("是否流量数据")
    private Boolean isLinkFlow;

    @ApiModelProperty("查询prometheus的数据")
    private List<MwVisualizedPrometheusDropDto> prometheusParam;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("分类模型属性索引")
    private String classIfyPropertiesIndexId;

    @ApiModelProperty("分类模型实例id")
    private Integer classIfyModelInstanceId;

    @ApiModelProperty("告警组件分区数据")
    List<QueryModelInstanceByPropertyIndexParam> propertyIndexParams;

    @ApiModelProperty("业务状态标题")
    private String businStatusTitle;

    @ApiModelProperty("是否查询当天告警")
    private Boolean isToDayAlert;

    @ApiModelProperty("告警趋势查询天数")
    private int alertTrendDays;

    @ApiModelProperty("是否过滤未监控资产")
    private Boolean isFilterMonitorFlag;

    @ApiModelProperty("需要展示的单位")
    private String units;

    @ApiModelProperty("线路ID")
    private List<String> linkIds;

    @ApiModelProperty("top几")
    private Integer topCount;

    @ApiModelProperty("过滤值")
    private String filterMaxValue;
}
