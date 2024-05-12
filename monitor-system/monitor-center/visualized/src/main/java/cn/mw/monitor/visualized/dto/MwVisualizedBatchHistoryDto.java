package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 历史趋势多线DTO
 * @date 2023/8/18 11:19
 */
@Data
@ApiModel("历史趋势多线DTO")
public class MwVisualizedBatchHistoryDto {

    @ApiModelProperty("折线名称")
    private String name;

    @ApiModelProperty("折线数据")
    private List<MwVisualizedCacheHistoryDto> historyDtos;
}
