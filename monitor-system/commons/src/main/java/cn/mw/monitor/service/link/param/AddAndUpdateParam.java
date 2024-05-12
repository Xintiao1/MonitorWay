package cn.mw.monitor.service.link.param;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/7/20 14:08
 */
@Data
public class AddAndUpdateParam {
    @ApiModelProperty("链路id")
    private String linkId;
    @ApiModelProperty("链路名称")
    private String linkName;
    @ApiModelProperty("链路描述")
    private String linkDesc;
    @ApiModelProperty("源资产名称")
    private String rootAssetsName;
    @ApiModelProperty("目标资产名称")
    private String targetAssetsName;
    @ApiModelProperty("源资产id")
    private String rootAssetsId;
    @ApiModelProperty("目标资产id")
    private String targetAssetsId;
    @ApiModelProperty("源资产非管理ip地址")
    private String rootIpAddress;
    @ApiModelProperty("目标资产非管理ip地址")
    private String targetIpAddress;
    @ApiModelProperty("源端口")
    private String rootPort;
    @ApiModelProperty("目标端口")
    private String targetPort;
    @ApiModelProperty("源zabbixid")
    private Integer rootServerId;
    @ApiModelProperty("目标zabbixid")
    private Integer targetServerId;
    @ApiModelProperty("上行带宽")
    private String upLinkBandwidth;
    @ApiModelProperty("下行带宽")
    private String downLinkBandwidth;
    @ApiModelProperty("扫描方式 （NQA  ICMP  IPSLA）")
    private String scanType;
    @ApiModelProperty("取值端口（源端口 ROOT  目标端口 TARGET）")
    private String valuePort;
    @ApiModelProperty("是否启动链路探测（ACTIVE 启动  DISACTIVE 禁用）")
    private String enable;
    @ApiModelProperty("带宽单位")
    private String bandUnit;

    @ApiModelProperty("目标资产的管理ip地址")
    private String linkTargetIp;

    @ApiModelProperty("目标资产的管理的监控服务器")
    private Integer monitorServerId;

    @ApiModelProperty(value = "目标资产的管理的轮训引擎")
    private String pollingEngine;

    private String rngineradio;

    private Integer contentsId;
    public void setPollingEngine(String pollingEngine) {
        if (pollingEngine == null || "".equals(pollingEngine) || "本机".equals(pollingEngine)) {
            this.rngineradio = "本机";
        } else {
            this.rngineradio = "自定义";
        }
        this.pollingEngine = pollingEngine;
    }

//    @ApiModelProperty("链路标签")
//    private List<LinkLabelDto> linkLabelDtos;

    @ApiModelProperty(value="标签列表")
    private List<MwAssetsLabelDTO> assetsLabel;

    @ApiModelProperty("源设备")
    private AssetsParam rootAssetsParam;
    @ApiModelProperty("目标设备")
    private AssetsParam targetAssetsParam;



    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    private List<UserDTO> principal;

    private List<OrgDTO> department;

    private List<GroupDTO> groups;

    @ApiModelProperty(value = "责任人")
    private List<Integer> userIds;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;

    private List<String> ids;

    private boolean aBoolean;

    @ApiModelProperty(value = "主机ID")
    private String assetsId;

    @ApiModelProperty(value = "主机IP")
    private String assetsIp;

    @ApiModelProperty(value = "线路名称集合")
    private List<String> linkNames;

}
