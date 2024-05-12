package cn.mw.monitor.scanrule.dto;

import cn.mw.monitor.scanrule.model.MwScanruleTable;
import cn.mw.monitor.service.scanrule.model.Perform;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MwScanruleDTO extends MwScanruleTable {

    private String scanruleName;

    private List<MwIpRangDTO> ipRangDTO;

    private List<MwIpAddressListDTO> ipAddressListDTO;

    private List<MwIpAddressesDTO> ipAddressesDTO;

    //"检查类型1：端口 2：SNMP 3:AGENT 4:ICMP"
    private Integer monitorMode;

    //snmpv1/v2
    private List<MwRulesnmpv1DTO> rulesnmpv1DTOs;

    //snmp集合
    private List<MwRulesnmpDTO> rulesnmpDTOs;

    //agent接口
    private List<MwAgentruleDTO> agentruleDTOs;

    //端口扫描
    private List<MwPortruleDTO> portruleDTOs;

    //ICMP扫描
    private List<MwIcmpruleDTO> icmpruleDTOList;

    //是否立即执行
    private Boolean execution;

    //扫描开始时间
    private Date scanStartDate;

    //扫描结束时间
    private Date scanEndDate;

    private Date createDate;

    private Date modificationDate;

    private Perform perform;

    //引擎id
    private String engineId;

}
