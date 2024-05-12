package cn.mw.monitor.link.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gengjb
 * @description 线路下拉内容
 * @date 2023/8/7 14:41
 */
@Data
@ApiModel("线路下拉内容DTO")
public class MwLinkDropDowmDto {

    @ApiModelProperty("主键ID")
    private Integer id;

    @ApiModelProperty("下拉ID")
    private Integer dropKey;

    @ApiModelProperty("下拉值")
    private String dropValue;
}
