package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 资产统计趋势DTO
 * @Author gengjb
 * @Date 2023/5/17 14:30
 * @Version 1.0
 **/
@Data
@ApiModel("资产统计趋势DTO")
public class MwVisualizedResourceAssetsTrendDto {

    @ApiModelProperty("资产类型名称")
    private String typeName;

    @ApiModelProperty("子类型数据")
    private List<TypeSubTypeDto> typeDtos;

    @Data
    public class TypeSubTypeDto{
        @ApiModelProperty("子类型名称")
        private String typeSubName;

        @ApiModelProperty("子类型资产数量")
        private Integer typeSubCount;
    }
}
