package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.server.api.dto.ItemTrendApplication;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName
 * @Description 排行DTO
 * @Author gengjb
 * @Date 2023/4/17 21:28
 * @Version 1.0
 **/
@Data
@ApiModel("排行DTO")
public class MwVisualizedModuleRankingDto {

    @ApiModelProperty("实例名称")
    private String name;

    @ApiModelProperty("值")
    private String value;

    @ApiModelProperty("排序值")
    private Double sortValue;

    public void extractFrom(String name, String value, Double sortValue){
        this.name = name;
        this.value = value;
        this.sortValue = sortValue;
    }
}
