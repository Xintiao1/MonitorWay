package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName
 * @Description 资产分布情况DTO
 * @Author gengjb
 * @Date 2023/5/18 19:34
 * @Version 1.0
 **/
@Data
@ApiModel("资产分布情况DTO")
public class MwVisualizedResourceAssetsSpreadDto {

    @ApiModelProperty("分布名称")
    private String name;

    @ApiModelProperty("分布明细")
    private List<SpreadDetailDto> spreadDetailDtos;

    @Data
    public class SpreadDetailDto{
        @ApiModelProperty("名称")
        private String typeName;

        @ApiModelProperty("子类型资产数量")
        private String value;

        @ApiModelProperty("数字类型值")
        private Double dValue;
    }
}
