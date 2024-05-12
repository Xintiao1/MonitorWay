package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MWModelZabbixMonitorDao;
import cn.mw.monitor.model.dto.MwModelSNMPV1V2;
import cn.mw.monitor.model.dto.MwModelSNMPV3;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.MwModelAuthProtocolType;
import cn.mw.monitor.model.param.MwModelPrivProtocolType;
import cn.mw.monitor.model.param.MwModelTPServerParam;
import cn.mw.monitor.model.service.MWModelZabbixMonitorService;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.service.assets.model.MwSnmpAssetsDTO;
import cn.mw.monitor.service.assets.model.MwSnmpv1AssetsDTO;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import cn.mw.monitor.service.model.param.MwModelAssetsInterfaceParam;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MWModelZabbixMonitorCommonService;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.scan.model.SecurityLevel;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;

/**
 * @author qzg
 * @date 2023/5/31
 */
@Service
@Slf4j
public class MWModelZabbixMonitorServiceImpl implements MWModelZabbixMonitorCommonService, MWModelZabbixMonitorService {
    @Resource
    private MWModelZabbixMonitorDao mwModelZabbixMonitorDao;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;

    /**
     * 修改接口的备注信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply updateInterfaceDescById(MwModelAssetsInterfaceParam param) {
        try {
            mwModelZabbixMonitorDao.updateInterfaceDescById(param);
        } catch (Exception e) {
            log.error("修改接口的备注信息失败", e);
            return Reply.fail(500, "修改接口的备注信息失败");
        }
        return Reply.ok();
    }

    /**
     * 设置接口数据是否显示
     *
     * @param param
     * @return
     */
    @Override
    public Reply batchUpdateInterfaceShow(MwModelAssetsInterfaceParam param) {
        try {
            //设置显示的接口
            mwModelZabbixMonitorDao.batchUpdateInterfaceShow(param);
            //设置隐藏的接口
            if (Strings.isNotBlank(param.getDeviceId())) {
                mwModelZabbixMonitorDao.batchUpdateInterfaceHide(param);
            }
        } catch (Exception e) {
            log.error("设置接口显示数据失败", e);
            return Reply.fail(500, "设置接口显示数据失败");
        }
        return Reply.ok();
    }

    @Override
    public Reply updateAlertTag(MwModelAssetsInterfaceParam param) {
        try {
            mwModelZabbixMonitorDao.updateAlertTag(param);
        } catch (Exception e) {
            log.error("设置接口告警标记失败", e);
            return Reply.fail(500, "设置接口告警标记失败");
        }
        return Reply.ok();
    }


    @Override
    public Reply updateInterfaceStatus() {
        try {
            mwModelZabbixMonitorDao.updateInterfaceStatus();
        } catch (Exception e) {
            log.error("updateInterfaceStatus fail to:", e);
            return Reply.fail(500, "设置接口显示标记失败");
        }
        return Reply.ok();
    }

    @Override
    public Reply queryMonitorServerInfo() {
        List<MwModelTPServerParam> serverList = new ArrayList<>();
        try {
            serverList = mwModelZabbixMonitorDao.queryMonitorServerInfo();
        } catch (Exception e) {
            log.error("获取监控服务器数据失败", e);
            return Reply.fail(500, "获取监控服务器数据失败");
        }
        return Reply.ok(serverList);
    }

    @Override
    public Reply syncAssetsDetailsToEs() {
        //获取SNMP资产信息
        List<Map<String, Object>> listMap = mwModelViewCommonService.getModelListInfoByPerm(QueryModelAssetsParam.builder()
                .monitorMode(2).filterQuery(true).build());

        List<Map<String, Object>> disMap = listMap.stream().filter(s -> s.get(ASSETS_ID) != null && !s.get(ASSETS_ID).toString().equals("") && s.get(MONITOR_SERVER_ID) != null && !"0".equals(s.get(MONITOR_SERVER_ID).toString()) &&
                (s.get(SNMPV1V2) == null && s.get(SNMPV3)==null)).collect(Collectors.toList());

        Map<String, Map<String, Object>> maps = disMap.stream().collect(Collectors.toMap(s -> s.get(ASSETS_ID).toString() + "_" + s.get(MONITOR_SERVER_ID).toString(), s -> s));

        Map<Integer, List<String>> groupMap = disMap.stream()
                .collect(Collectors.groupingBy(s -> Integer.valueOf(s.get(MONITOR_SERVER_ID).toString()), Collectors.mapping(s -> s.get(ASSETS_ID).toString(), Collectors.toList())));
        List<AddAndUpdateModelInstanceParam> addInstanceParams = new ArrayList<>();
        groupMap.forEach((key, value) -> {
            MWZabbixAPIResult hostDetailsInfo = mwtpServerAPI.getHostDetailsByHostIds(key, value);
            JsonNode data = (JsonNode) hostDetailsInfo.getData();
            if (data.size() > 0) {
                for (JsonNode host : data) {
                    String hostId = host.get("hostid").asText();
                    MwModelSNMPV3 snmpv3 = null;
                    MwModelSNMPV1V2 snmpv1 = null;
                    MwSnmpv1AssetsDTO mwSnmpv1AssetsDTO = new MwSnmpv1AssetsDTO();
                    MwSnmpAssetsDTO mwSnmpAssetsDTO = new MwSnmpAssetsDTO();
                    if (host.get("interfaces").size() > 0) {
                        JsonNode interfaceInfos = host.get("interfaces").get(0);
                        String port = interfaceInfos.get("port").asText();
                        JsonNode details = interfaceInfos.get("details");
                        String version = details.get("version").asText();
                        if ("3".equals(version)) {
                            String securityname = details.get("securityname").asText();
                            String securitylevel = details.get("securitylevel").asText();
                            String authpassphrase = details.get("authpassphrase").asText();
                            String privpassphrase = details.get("privpassphrase").asText();
                            String authprotocol = details.get("authprotocol").asText();
                            String privprotocol = details.get("privprotocol").asText();
                            String contextname = details.get("contextname").asText();
                            if (Strings.isNotEmpty(securitylevel)) {
                                mwSnmpAssetsDTO.setSecLevelName(SecurityLevel.getName(Integer.valueOf(securitylevel)));
                                mwSnmpAssetsDTO.setSecLevel(Integer.valueOf(securitylevel));
                            }
                            if (Strings.isNotEmpty(authprotocol)) {
                                mwSnmpAssetsDTO.setAuthAlgName(MwModelAuthProtocolType.getName(Integer.valueOf(authprotocol)));
                                mwSnmpAssetsDTO.setAuthAlg(Integer.valueOf(authprotocol));
                            }
                            if (Strings.isNotEmpty(privprotocol)) {
                                mwSnmpAssetsDTO.setPrivAlgName(MwModelPrivProtocolType.getName(Integer.valueOf(privprotocol)));
                                mwSnmpAssetsDTO.setPrivAlg(Integer.valueOf(privprotocol));
                            }
                            mwSnmpAssetsDTO.setAuthValue(authpassphrase);
                            mwSnmpAssetsDTO.setPriValue(privpassphrase);
                            mwSnmpAssetsDTO.setSecName(securityname);
                            mwSnmpAssetsDTO.setContextName(contextname);
                            mwSnmpAssetsDTO.setPort(Integer.valueOf(port));
                        } else {
                            String community = details.get("community").asText();
                            mwSnmpv1AssetsDTO.setCommunity(community);
                            mwSnmpv1AssetsDTO.setSnmpVersion(Integer.valueOf(version));
                            mwSnmpv1AssetsDTO.setPort(Integer.valueOf(port));
                        }
                    }
                    String mapKey = hostId + "_" + key;
                    if (maps != null && maps.containsKey(mapKey)) {
                        Map<String, Object> ms = maps.get(mapKey);
                        String modelIndex = ms.get(MODEL_INDEX).toString();
                        String instanceId = ms.get(INSTANCE_ID_KEY).toString();
                        String esId = ms.get(ESID).toString();
                        if (Strings.isEmpty(esId)) {
                            esId = modelIndex + instanceId;
                        }
                        List<AddModelInstancePropertiesParam> propertiesInsertList = new ArrayList<>();
                        AddAndUpdateModelInstanceParam addParam = new AddAndUpdateModelInstanceParam();
                        AddModelInstancePropertiesParam propertiesInsertParam = new AddModelInstancePropertiesParam();
                        if (null != mwSnmpv1AssetsDTO && null != mwSnmpv1AssetsDTO.getPort()) {
                            propertiesInsertParam = new AddModelInstancePropertiesParam();
                            if (null != instanceId) {
                                mwSnmpv1AssetsDTO.setAssetsId(instanceId.toString());
                            }
                            propertiesInsertParam.setPropertiesIndexId(MwModelViewCommonService.SNMPV1V2);
                            propertiesInsertParam.setPropertiesValue(genStructData(mwSnmpv1AssetsDTO));
                            propertiesInsertParam.setPropertiesType(ModelPropertiesType.STRUCE.getCode());
                            propertiesInsertParam.setPropertiesName(MwModelViewCommonService.SNMPV1V2);
                        }
                        if (null != mwSnmpAssetsDTO && null != mwSnmpAssetsDTO.getPort()) {
                            propertiesInsertParam = new AddModelInstancePropertiesParam();
                            if (null != instanceId) {
                                mwSnmpAssetsDTO.setAssetsId(instanceId.toString());
                            }
                            propertiesInsertParam.setPropertiesIndexId(MwModelViewCommonService.SNMPV3);
                            propertiesInsertParam.setPropertiesValue(genStructData(mwSnmpAssetsDTO));
                            propertiesInsertParam.setPropertiesType(ModelPropertiesType.STRUCE.getCode());
                            propertiesInsertParam.setPropertiesName(MwModelViewCommonService.SNMPV3);
                        }
                        propertiesInsertList.add(propertiesInsertParam);
                        addParam.setPropertiesList(propertiesInsertList);
                        addParam.setModelIndex(modelIndex);
                        addParam.setEsId(esId);
                        addInstanceParams.add(addParam);
                    }
                }
            }

        });
        //更新es数据
        mwModelInstanceService.batchUpdateModelInstance(addInstanceParams);
        return Reply.ok();
    }

    private String genStructData(Object obj) {
        List<Object> ret = new ArrayList<>();
        ret.add(obj);
        return JSON.toJSONString(ret);
    }
}
