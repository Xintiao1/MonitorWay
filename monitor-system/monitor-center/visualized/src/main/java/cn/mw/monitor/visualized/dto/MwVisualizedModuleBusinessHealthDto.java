package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName
 * @Description 业务健康状态DTO
 * @Author gengjb
 * @Date 2023/4/17 23:12
 * @Version 1.0
 **/
@Data
@ApiModel("业务健康状态DTO")
public class MwVisualizedModuleBusinessHealthDto {

    @ApiModelProperty("类型分类统计")
    private List<MwVisualizedScoreProportionDto> typeClassifys;

    @ApiModelProperty("业务分类明细")
    private Map<String, List<MwVisualizedModuleModelTypeDto>> businessClassify;
}
