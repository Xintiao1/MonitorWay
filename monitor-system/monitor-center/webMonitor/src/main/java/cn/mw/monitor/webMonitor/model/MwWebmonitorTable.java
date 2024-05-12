package cn.mw.monitor.webMonitor.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class MwWebmonitorTable {
    @ApiModelProperty("zibbix返回的ID")
    private String httpTestId;

    private Integer id;

    @ApiModelProperty("网站名称")
    private String webName;

    @ApiModelProperty("网站url")
    private String webUrl;

    @ApiModelProperty("IP地址类型")
    private String ipType;

    @ApiModelProperty("调用的服务器ID")
    private String hostId;

    @ApiModelProperty("更新间隔")
    private Integer updateInterval;

    @ApiModelProperty("尝试次数")
    private Integer attempts;

    @ApiModelProperty("客户端")
    private Integer client;

    @ApiModelProperty("http代理")
    private String httpProxy = "";

    @ApiModelProperty("启用状态")
    private String enable;

    @ApiModelProperty("跟随跳转")
    private Boolean followJump;

    @ApiModelProperty("超时")
    private Integer timeOut;

    @ApiModelProperty("必要状态码")
    private String statusCode;

    @ApiModelProperty("必要字符串")
    private String string;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("修改人")
    private String modifier;

    @ApiModelProperty("修改时间")
    private Date modificationDate;

    @ApiModelProperty("去查zabbix的hostId")
    private String assetsId;
}
