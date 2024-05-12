package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 评分占比DTO
 * @date 2023/9/27 10:15
 */
@Data
@ApiModel("评分占比DTO")
public class MwVisualizedScoreProportionDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("评分占比")
    private Integer proportion;

    @ApiModelProperty("分类类型，1：业务分类，2：资产类型，3：资产名称")
    private Integer type;

    @ApiModelProperty("分类名称")
    private String classifyName;

    @ApiModelProperty("监控名称")
    private String itemName;

    @ApiModelProperty("属于该分类的资产信息")
    private List<MwTangibleassetsDTO> assetsDtos;

    @ApiModelProperty("资产数量")
    private Integer assetsCount;

    @ApiModelProperty("分类状态")
    private String status;

    @ApiModelProperty("问题资产")
    private String errorAssets;
}
