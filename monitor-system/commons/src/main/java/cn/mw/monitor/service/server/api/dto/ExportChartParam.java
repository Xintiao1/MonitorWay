package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author syt
 * @Date 2021/4/6 10:30
 * @Version 1.0
 */
@Data
public class ExportChartParam {

    //导出的数据有那些字段
    private Set<String> fields;

    //导出文件名
    private String name;

    //折线图展示数据
    private List<HistoryListDto> list;
    //数据颗粒度
    private int delay;

    @ApiModelProperty("当时间类型为 5:自定义时,开始时间")
    private String dateStart;
    @ApiModelProperty("当时间类型为 5:自定义时,结束时间")
    private String dateEnd;

    private Integer dateType;
    //监控服务器id
    private int monitorServerId;
    //主机id
    private String assetsId;
    //监控项名称
    private List<String> itemNames;
    //监控项信息
    private List<ItemApplication> itemsList;

    private String webName;
    @ApiModelProperty("监控类型,1:下载速度,2:响应时间")
    private String titleType;

    @ApiModelProperty("是否取趋势数据")
    private Boolean isTrend;

    @ApiModelProperty("取值类型,AVG:平均值 MAX:最大值 MIN:最小值")
    private String valueType;

    @ApiModelProperty("主机名称")
    private String hostName;
}
