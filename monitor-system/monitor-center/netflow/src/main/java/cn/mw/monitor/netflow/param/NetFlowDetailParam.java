package cn.mw.monitor.netflow.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className NetFlowDetailParam
 * @description 流量明细请求参数
 * @date 2023/2/14
 */
@Data
@ApiModel("流量明细请求参数")
public class NetFlowDetailParam extends BaseParam {
    /**
     * kibana请求格式
     */
    @ApiModelProperty(value = "kibana请求格式")
    private List<KibanaPageParam> kibanaList;

    /**
     * 时间分类类型
     */
    @ApiModelProperty("时间分类类型")
    private Integer type;

    /**
     * 时间取值类型
     */
    @ApiModelProperty("时间取值类型")
    private Integer dateType;

    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    private TimeParam startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    private TimeParam endTime;

    /**
     * 开始时间
     */
    private Date startDateTime;

    /**
     * 结束时间
     */
    private Date endDateTime;

    /**
     * 排序字段
     */
    private String sortColumn;

    /**
     * 排序方式（desc 或者 asc）
     */
    private String sortType;

    public boolean dateParamIsNotEmpty(){
        return startTime != null && endTime != null;
    }
}
