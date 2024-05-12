package cn.mw.monitor.scanrule.api.param.scanrule;

import cn.mw.monitor.scanrule.dto.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
@ApiModel(value = "新增规则表")
public class AddScanruleParam{
    private Integer id;
    /**
     * 规则名称
     */
    @ApiModelProperty(value="规则名称")
    private String scanruleName;

    @ApiModelProperty(value="IP范围")
    private List<MwIpRangDTO> mwIpRangDTO;

    @ApiModelProperty(value="IP列表")
    private List<MwIpAddressListDTO> ipAddressListDTO;

    @ApiModelProperty(value="IP地址段")
    private List<MwIpAddressesDTO> ipAddressesDTO;

    @ApiModelProperty(value = "检查类型1：端口 2：SNMP 3:AGENT 4:ICMP")
    private Integer monitorMode;

    @ApiModelProperty(value="snmpv1/v2")
    private List<MwRulesnmpv1DTO> rulesnmpv1DTOList;

    @ApiModelProperty(value="snmpv3")
    private List<MwRulesnmpDTO> rulesnmpDTOList;

    @ApiModelProperty(value="agent接口")
    private List<MwAgentruleDTO> agentruleDTOList;

    @ApiModelProperty(value="端口扫描")
    private List<MwPortruleDTO> portruleDTOList;

    @ApiModelProperty(value="ICMP扫描")
    private List<MwIcmpruleDTO> icmpruleDTOList;

    @ApiModelProperty(value="是否立即执行")
    private Boolean execution;

    @ApiModelProperty(value="开始时间")
    private Date createDate;

    @ApiModelProperty(value="结束时间")
    private Date modificationDate;

    @ApiModelProperty(value="执行者")
    private String creator;

    private String modifier;

    @ApiModelProperty(value="扫描开始时间")
    private Date scanStartDate;

    @ApiModelProperty(value="扫描结束时间")
    private Date scanEndDate;

    private Integer deleteFlag;

    @ApiModelProperty(value="监控服务器id")
    private Integer monitorServerId;

    @ApiModelProperty(value="引擎id")
    private String engineId;
}
