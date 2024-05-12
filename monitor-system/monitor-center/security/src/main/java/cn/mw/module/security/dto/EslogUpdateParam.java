package cn.mw.module.security.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/9/7 15:16
 */
@Data
public class EslogUpdateParam {
    @ApiModelProperty("index")
    private String index;
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("告警等级")
    private String alertLevel;
    @ApiModelProperty("处理建议")
    private String disposalSuggestion;
    @ApiModelProperty("处理状态  0未处理  1已处理")
    private String disposalStatus;
    @ApiModelProperty("域名")
    private String destDomain;
    @ApiModelProperty("是否备案")
    private String isRecord;
    @ApiModelProperty("是否测试")
    private String isTest;

}
