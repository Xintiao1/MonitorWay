package cn.mw.monitor.api.controller.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/4/24 17:42
 */
@Data
public class WebDto {
    @ApiModelProperty("httptestids")
    private String httptestids;
    @ApiModelProperty("web名称")
    private String name;
    @ApiModelProperty("绑定的主机id")
    private String hostId;
    @ApiModelProperty("代理人 默认Zabbix")
    private String agent;
    @ApiModelProperty("延迟时间 1m")
    private String delay;
    @ApiModelProperty("是否启用web 0启用 1禁用")
    private Integer status;
    @ApiModelProperty("web失败重试次数")
    private Integer retries;//web失败重试次数
    @ApiModelProperty("步骤")
    private List<Step> steps;

    @Data
    public class Step {
        @ApiModelProperty("名称")
        private String name;
        @ApiModelProperty("步骤序列号")
        private Integer no;
        @ApiModelProperty("网址")
        private String url;
        @ApiModelProperty("端口号")
        private String posts;
        @ApiModelProperty("状态码")
        private String status_codes;
        @ApiModelProperty("超时时间 默认15s")
        private String timeout;
    }

}
