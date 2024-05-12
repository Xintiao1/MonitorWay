package cn.mw.monitor.service.server.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author gengjb
 * @description 趋势数据明细
 * @date 2024/1/12 11:14
 */
@Data
public class ItemTrendGroupDto {

    @ApiModelProperty("数据ID")
    private String id;

    @ApiModelProperty("数据明细")
    private Map<String, List<ItemTrendDto>> trendMap;
}
