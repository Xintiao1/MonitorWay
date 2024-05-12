package cn.mw.monitor.service.alert.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/7/29 10:37
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWItemDto {
    private String itemId;
    private String name;
    private String ename;//英文名称
    private Integer valueType;
    private String units;
    @ApiModelProperty("告警时间")
    private String clock;
    private Integer monitorServerId;
    private Date startDate;
    private Date endDate;
    @ApiModelProperty("时间类型，1：hour 2:day 3:week 4:month 5:自定义")
    private Integer dateType;
}
