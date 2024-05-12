package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName
 * @Description zabbix趋势查询实体
 * @Author gengjb
 * @Date 2023/4/18 14:05
 * @Version 1.0
 **/
@Data
public class ItemTrendApplication {

    @ApiModelProperty("监控项ID")
    private String itemid;

    @ApiModelProperty("数据产生时间 单位：秒")
    private String clock;

    @ApiModelProperty("每小时产生的值数量")
    private String num;

    @ApiModelProperty("每小时最大值")
    private String value_max;

    @ApiModelProperty("每小时平均值")
    private String value_avg;

    @ApiModelProperty("每小时最小值")
    private String value_min;
}
