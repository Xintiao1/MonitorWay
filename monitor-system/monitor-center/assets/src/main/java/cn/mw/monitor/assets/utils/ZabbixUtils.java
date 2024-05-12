package cn.mw.monitor.assets.utils;

import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.service.model.exception.NotFindGroupException;
import cn.mw.monitor.service.model.exception.NotFindTemplateException;
import cn.mw.monitor.assets.model.TangibleAssetMonitorState;
import cn.mw.monitor.assets.model.TangibleAssetState;
import cn.mw.monitor.service.assets.model.MwAgentAssetsDTO;
import cn.mw.monitor.service.assets.model.MwPortAssetsDTO;
import cn.mw.monitor.service.assets.model.MwSnmpAssetsDTO;
import cn.mw.monitor.service.assets.model.MwSnmpv1AssetsDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.Macros;
import cn.mw.monitor.service.assets.param.MwIPMIAssetsDTO;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.assets.utils.VersionType;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.engineManage.dto.MwEngineManageDTO;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mw.monitor.service.scan.model.SecurityLevel;
import cn.mw.monitor.service.scan.model.SecurityProtocolType;
import cn.mw.monitor.service.zbx.model.HostCreateParam;
import cn.mw.monitor.util.ListMapObjUtils;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ZabbixUtils {

    @Autowired
    private MwEngineCommonsService engineCommonsService;

    public List<HostCreateParam> transform(List<AddUpdateTangAssetsParam> aParams) throws Exception {
        List<HostCreateParam> hostCreateParams = new ArrayList<>();

        //获取engine信息
        List<String> engineIds = new ArrayList<>();
        for (AddUpdateTangAssetsParam param : aParams) {
            if (!engineIds.contains(param.getPollingEngine())) {
                engineIds.add(param.getPollingEngine());
            }
        }

        Map proxyMap = new HashMap();
        if (engineIds.size() > 0) {
            List<MwEngineManageDTO> proxyEntitys = engineCommonsService.selectEngineByIdsNoPerm(engineIds);
            proxyMap = proxyEntitys.stream().collect(Collectors.toMap(MwEngineManageDTO::getId, Function.identity()));
        }

        for (AddUpdateTangAssetsParam param : aParams) {
            HostCreateParam hostCreateParam = transform(param, proxyMap);
            hostCreateParams.add(hostCreateParam);
        }

        return hostCreateParams;

    }

    public HostCreateParam transform(AddUpdateTangAssetsParam aParam, Map<String, MwEngineManageDTO> engineMap) throws Exception {
        HostCreateParam hostCreateParam = new HostCreateParam();
        hostCreateParam.setBatchIndex(aParam.getBatchIndex());
        if (aParam.getTPServerHostName() == null || StringUtils.isEmpty(aParam.getTPServerHostName())) {
            //设置第三方监控服务器中主机名称
            aParam.setTPServerHostName(UuidUtil.getUid());
        }
        //获取分组Id
        ArrayList<String> groupIdList = new ArrayList<>();
        String groupId = !Strings.isNullOrEmpty(aParam.getGroupId()) ? aParam.getGroupId() : aParam.getHostGroupId();
        if(Strings.isNullOrEmpty(groupId)){
            throw new NotFindGroupException();
        }
        groupIdList.add(groupId);
        ArrayList<Map<String, Object>> interList = new ArrayList<>();
        Map interMap = new HashMap();
        interMap.put("ip", aParam.getInBandIp());
        interMap.put("main", 1);
        String port = "";
        String community = "";
        int interfaceType = 1;

        Map detailsMap = new HashMap();
        List<Map> macroDTOS = new ArrayList<>();

        RuleType ruleType = RuleType.getInfoByMonitorMode(aParam.getMonitorMode());
        if (null != aParam.getMwMacrosDTO() && aParam.getMwMacrosDTO().size() > 0) {
            for (Macros macro : aParam.getMwMacrosDTO()) {
                if (macro.getType() == 1) {
                    macro.setValue(RSAUtils.decryptData(macro.getValue(), RSAUtils.RSA_PRIVATE_KEY));
                }
            }
            macroDTOS = ListMapObjUtils.objectsToMaps(aParam.getMwMacrosDTO());

        }
        //使用默认端口
        interfaceType = aParam.getInterfacesType() > 0 ? aParam.getInterfacesType() : ruleType.geInterfaceType();
        port = (null != aParam.getMonitorPort() && StringUtils.isNotEmpty(aParam.getMonitorPort().toString()))
                ? aParam.getMonitorPort().toString() : ruleType.getPort();
        String userName = "";
        String passWord = "";
        switch (ruleType) {
            case ZabbixAgent:
                MwAgentAssetsDTO agentAssetsDTO = aParam.getAgentAssetsDTO();
                if (agentAssetsDTO != null && agentAssetsDTO.getPort() != null) {
                    port = String.valueOf(agentAssetsDTO.getPort());
                }
                break;
            case SNMPv1v2:
                RuleType rt = RuleType.valueOf(aParam.getVersion());
                switch (rt) {
                    case SNMPv1v2:
                        if (aParam.getSnmpV1AssetsDTO() != null && aParam.getSnmpV1AssetsDTO().getPort() != null) {
                            port = String.valueOf(aParam.getSnmpV1AssetsDTO().getPort());
                        }
                        community = aParam.getSnmpV1AssetsDTO().getCommunity();
                        Integer snmpVersion = aParam.getSnmpV1AssetsDTO().getSnmpVersion();
                        if (snmpVersion == null) {
                            detailsMap.put("version", 2);
                        } else {
                            VersionType versionType = VersionType.getZabbixVersionBySnmpVersion(snmpVersion);
                            detailsMap.put("version", versionType.getZabbixVersion());
                        }
                        detailsMap.put("bulk", 1);
                        detailsMap.put("community", community);

                        Map map = new HashMap();
                        map.put("macro", "{$SNMP_COMMUNITY}");
                        map.put("value", community);
                        macroDTOS.add(map);
                        break;
                    case SNMPv3:
                        if (aParam.getSnmpAssetsDTO() != null && aParam.getSnmpAssetsDTO().getPort() != null) {
                            port = String.valueOf(aParam.getSnmpAssetsDTO().getPort());
                        }
                        detailsMap.put("version", 3);
                        detailsMap.put("bulk", 1);
                        detailsMap.put("securityname", aParam.getSnmpAssetsDTO().getSecName());
                        SecurityLevel securityLevel = SecurityLevel.valueOf(aParam.getSnmpAssetsDTO().getSecLevelName());
                        detailsMap.put("securitylevel", securityLevel.getCode());
                        detailsMap.put("contextname", aParam.getSnmpAssetsDTO().getContextName());

                        switch (securityLevel) {
                            case noAuthNoPriv:
                                break;
                            case authPriv:
                                MwSnmpAssetsDTO snmpAssetsDTO = aParam.getSnmpAssetsDTO();
                                SecurityProtocolType authType = SecurityProtocolType.valueOf(snmpAssetsDTO.getAuthAlgName());
                                SecurityProtocolType privType = SecurityProtocolType.valueOf(snmpAssetsDTO.getPrivAlgName());
                                detailsMap.put("authprotocol", authType.getInterfaceCode());
                                detailsMap.put("privprotocol", privType.getInterfaceCode());
                                detailsMap.put("authpassphrase", snmpAssetsDTO.getAuthValue());
                                detailsMap.put("privpassphrase", snmpAssetsDTO.getPriValue());
                                break;
                            case authNoPriv:
                                MwSnmpAssetsDTO snmpAssetsDTO1 = aParam.getSnmpAssetsDTO();
                                SecurityProtocolType authType1 = SecurityProtocolType.valueOf(snmpAssetsDTO1.getAuthAlgName());
                                detailsMap.put("authprotocol", authType1.getInterfaceCode());
                                detailsMap.put("authpassphrase", snmpAssetsDTO1.getAuthValue());
                                break;
                            default:
                        }
                        break;
                    default:
                }
                break;
            case IPMI:
                MwIPMIAssetsDTO ipmiAssetsDTO = aParam.getMwIPMIAssetsDTO();
                if (ipmiAssetsDTO != null && ipmiAssetsDTO.getPort() != null) {
                    port = String.valueOf(ipmiAssetsDTO.getPort());
                }
                userName = ipmiAssetsDTO.getAccount();
                passWord = ipmiAssetsDTO.getPassword();
                break;
            default:
                log.warn("no match MonitorMode" + aParam.getMonitorMode());
        }

        interMap.put("dns", "");
        interMap.put("port", port);
        interMap.put("type", interfaceType);//type 接口类型  1 - agent 2 - SNMP;3 - IPMI;4 - JMX.
        interMap.put("useip", 1);//使用的链接方式 0 DNS名称连接 1 IP地址进行连接
        if (detailsMap.size() > 0) {
            interMap.put("details", detailsMap);
        }
        interList.add(interMap);

        ArrayList<String> templList = new ArrayList<>();
        if(Strings.isNullOrEmpty(aParam.getTemplateId())){
            throw new NotFindTemplateException();
        }
        templList.add(aParam.getTemplateId());
        Integer status = TangibleAssetMonitorState.FALSE.getZabbixStatus();
        if (aParam.getMonitorFlag() != null) {
            status = aParam.getMonitorFlag()
                    ? TangibleAssetMonitorState.TRUE.getZabbixStatus() : TangibleAssetMonitorState.FALSE.getZabbixStatus();
        }

        //获取代理ip
        String proxyId = null;
        if (null == engineMap) {
            String pollId = aParam.getPollingEngine();
            List<String> ids = new ArrayList<>();
            ids.add(pollId);
            List<MwEngineManageDTO> proxyEntitys = engineCommonsService.selectEngineByIdsNoPerm(ids);
            if (null != proxyEntitys && proxyEntitys.size() > 0) {
                proxyId = proxyEntitys.get(0).getProxyId();
            }
        } else {
            MwEngineManageDTO data = engineMap.get(aParam.getPollingEngine());
            if (null != data) {
                proxyId = data.getProxyId();
            }
        }
        hostCreateParam.setServerId(aParam.getMonitorServerId());
        //主机名称设置
        Pattern p = Pattern.compile("[0-9a-zA-Z_\\. \\-]+");
        String visibleName = !Strings.isNullOrEmpty(aParam.getInstanceName()) ? aParam.getInstanceName() : aParam.getHostName();
        hostCreateParam.setHost(aParam.getTPServerHostName());
        hostCreateParam.setGroups(groupIdList);
        hostCreateParam.setInterfaces(interList);
        hostCreateParam.setTemplates(templList);
        hostCreateParam.setMacros(macroDTOS);
        hostCreateParam.setStatus(status);
        hostCreateParam.setProxyID(proxyId);
        hostCreateParam.setUserName(userName);
        hostCreateParam.setPassWord(passWord);
        //设置可见名称
        hostCreateParam.setVisibleName(visibleName+UuidUtil.get16Uid());
        return hostCreateParam;
    }

    public AddUpdateTangAssetsParam tranform(ScanResultSuccess scanResultSuccess) {
        AddUpdateTangAssetsParam aParam = new AddUpdateTangAssetsParam();
        String assetName = scanResultSuccess.getHostName() + "_" + UuidUtil.get16Uid();
        aParam.setAssetsName(assetName);
        aParam.setPollingEngine(scanResultSuccess.getPollingEngine());
        aParam.setHostName(scanResultSuccess.getHostName());
        aParam.setAssetsTypeId(scanResultSuccess.getAssetsTypeId());
        aParam.setAssetsTypeSubId(scanResultSuccess.getAssetsSubTypeId());
        aParam.setEnable(TangibleAssetState.ACTIVE.name());
        aParam.setManufacturer(scanResultSuccess.getBrand());
        aParam.setDescription(scanResultSuccess.getDescription());
        aParam.setSpecifications(scanResultSuccess.getSpecifications());
        aParam.setHostGroupId(scanResultSuccess.getGroupId());
        aParam.setTemplateId(scanResultSuccess.getTemplateId());
        aParam.setSettingFlag(true);
        aParam.setInBandIp(scanResultSuccess.getIpAddress());
        aParam.setScanSuccessId(scanResultSuccess.getId());
        aParam.setMonitorServerId(scanResultSuccess.getMonitorServerId());
        aParam.setDeviceCode(scanResultSuccess.getDeviceCode());
        aParam.setUserIds(scanResultSuccess.getUserIds());
        aParam.setOrgIds(scanResultSuccess.getOrgIds());
        aParam.setGroupIds(scanResultSuccess.getGroupIds());
        aParam.setInstanceName(Strings.isNullOrEmpty(scanResultSuccess.getInstanceName())?scanResultSuccess.getHostName():scanResultSuccess.getInstanceName());
        RuleType rt = RuleType.valueOf(scanResultSuccess.getMonitorMode());
        switch (rt) {
            case SNMPv1v2:
                MwSnmpv1AssetsDTO snmpV1AssetsDTO = new MwSnmpv1AssetsDTO();
                snmpV1AssetsDTO.setPort(Integer.parseInt(scanResultSuccess.getPort()));
                snmpV1AssetsDTO.setCommunity(scanResultSuccess.getCommunity());
                snmpV1AssetsDTO.setSnmpVersion(scanResultSuccess.getSnmpVersion());
                aParam.setSnmpV1AssetsDTO(snmpV1AssetsDTO);
                break;
            case SNMPv3:
                MwSnmpAssetsDTO snmpAssetsDTO = new MwSnmpAssetsDTO();
                snmpAssetsDTO.setPort(Integer.parseInt(scanResultSuccess.getPort()));
                snmpAssetsDTO.setSecName(scanResultSuccess.getSecurityName());
                snmpAssetsDTO.setContextName(scanResultSuccess.getContextName());

                aParam.setSnmpLev(scanResultSuccess.getSecurityLevel().getCode());

                snmpAssetsDTO.setSecLevelName(scanResultSuccess.getSecurityLevel().name());
                snmpAssetsDTO.setAuthAlg(scanResultSuccess.getAuthProtocol().getDropDownMapCode());
                snmpAssetsDTO.setAuthAlgName(scanResultSuccess.getAuthProtocol().name());
                snmpAssetsDTO.setAuthValue(scanResultSuccess.getAuthToken());
                snmpAssetsDTO.setPrivAlg(scanResultSuccess.getPrivProtocol().getDropDownMapCode());
                snmpAssetsDTO.setPrivAlgName(scanResultSuccess.getPrivProtocol().name());
                snmpAssetsDTO.setPriValue(scanResultSuccess.getPrivToken());
                aParam.setSnmpAssetsDTO(snmpAssetsDTO);
                break;
            case ZabbixAgent:
                MwAgentAssetsDTO agentAssetsDTO = new MwAgentAssetsDTO();
                agentAssetsDTO.setPort(Integer.parseInt(scanResultSuccess.getPort()));
                aParam.setAgentAssetsDTO(agentAssetsDTO);
                break;
            case Port:
                MwPortAssetsDTO portAssetsDTO = new MwPortAssetsDTO();
                portAssetsDTO.setPort(Integer.parseInt(scanResultSuccess.getPort()));
                aParam.setPortAssetsDTO(portAssetsDTO);
                aParam.setMonitorPort(Integer.parseInt(scanResultSuccess.getMonitorPort()));
                break;
            default:
                log.warn("no match RuleType!" + scanResultSuccess.getMonitorMode());
        }
        aParam.setMonitorMode(rt.getMonitorMode());
        aParam.setVersion(scanResultSuccess.getMonitorMode());
        aParam.setMonitorFlag(scanResultSuccess.getMonitorFlag());
        aParam.setMonitorModeName(scanResultSuccess.getMonitorModeName());
        aParam.setAssetsTypeName(scanResultSuccess.getAssetsTypeName());
        aParam.setAssetsSubTypeName(scanResultSuccess.getAssetsSubTypeName());
        aParam.setMonitorServerName(scanResultSuccess.getMonitorServerName());
        aParam.setPollingEngineName(scanResultSuccess.getPollingEngineName());
        //设置第三方监控服务器中主机名称
        aParam.setTPServerHostName(UuidUtil.getUid());
        return aParam;
    }

}
