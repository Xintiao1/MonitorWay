package cn.mw.monitor.service.server.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author gengjb
 * @description 查询历史趋势数据参数
 * @date 2024/1/2 8:51
 */
@Data
public class QueryItemTrendParam {

    @ApiModelProperty("数据类型,1:线路，2：资产")
    private Integer dataType;

//    @ApiModelProperty("时间类型,1:今日，2：昨日")
//    private Integer dateType;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("数据ID")
    private List<String> ids;

    @ApiModelProperty("查询的监控项")
    private List<String> itemNames;

    @ApiModelProperty("资产类型名称")
    private String assetsTypeName;

    @ApiModelProperty("IP对应关系")
    private Map<String,String> idMap;

    @ApiModelProperty("机房名称")
    private List<String> roomNames;
}
