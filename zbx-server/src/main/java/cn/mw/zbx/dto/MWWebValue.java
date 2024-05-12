package cn.mw.zbx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/25 19:38
 */
@Data
public class MWWebValue {
    @ApiModelProperty("网站状态")
    private String state;
    @ApiModelProperty("下载速度")
    private String bps;
    @ApiModelProperty("响应时间")
    private String resp;
    @ApiModelProperty("重试次数")
    private String error;
    @ApiModelProperty("返回状态码")
    private String rcode;

}
