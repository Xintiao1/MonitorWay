package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MwModelWebMonitorTable {
    @ApiModelProperty("zibbix返回的ID")
    private String httpTestId;

    private Integer id;

    @ApiModelProperty("网站名称")
    private String instanceName;

    @ApiModelProperty("网站url")
    private String webUrl;

    @ApiModelProperty("IP地址类型")
    private String ipType;

    @ApiModelProperty("调用的服务器ID")
    private String hostId;

    @ApiModelProperty("调用的服务器IP")
    private String inBandIp;

    @ApiModelProperty("响应时间")
    private String responseTime;

    @ApiModelProperty("返回代码")
    private String monitorCode;

    @ApiModelProperty("超时时间")
    private String fullTimeOut;

    @ApiModelProperty("web监测状态")
    private String webState;

    @ApiModelProperty("更新间隔")
    private Integer updateInterval;

    @ApiModelProperty("尝试次数")
    private Integer attempts;

    @ApiModelProperty("客户端")
    private Integer client;

    @ApiModelProperty("http代理")
    private String httpProxy = "";

    @ApiModelProperty("启用状态")
    private Boolean enable;

    @ApiModelProperty("跟随跳转")
    private Boolean followJump;

    @ApiModelProperty("下载速度")
    private String downloadSpeed;

    @ApiModelProperty("下载速度")
    private long sortDownloadSpeed;

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

    @ApiModelProperty("zabbix主机名称")
    private String assetsName;

    @ApiModelProperty(value = "是否监控")
    private Boolean isManage;

    //web监测 模型实例使用字段
    private Integer monitorServerId;
    private String modelId;
    private String esId;
    private String assetsTypeId;
    private String assetsTypeName;
    private String assetsTypeSubId;
    private String assetsTypeSubName;
    private String groupNodes;
    private String modelIndex;
    private Integer modelInstanceId;
    private List<Integer> userIds;
    private List<List<Integer>> orgIds;
    private List<Integer> groupIds;

}
