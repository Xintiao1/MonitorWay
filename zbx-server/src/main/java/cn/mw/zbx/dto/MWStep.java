package cn.mw.zbx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/4/26
 */
@Data
@Builder
public class MWStep {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("步骤序列号")
    private Integer no;
    @ApiModelProperty("网址")
    private String url;
    @ApiModelProperty("状态码")
    private String status_codes;
    @ApiModelProperty("超时时间 默认15s")
    private String timeout;
    @ApiModelProperty("必要的字段")
    private String required;
    @ApiModelProperty("跟随跳转")
    private Integer followRedirects;
}
