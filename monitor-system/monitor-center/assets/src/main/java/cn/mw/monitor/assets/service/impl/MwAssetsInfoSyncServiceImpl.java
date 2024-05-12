package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dao.MwAssetsInfoSyncDao;
import cn.mw.monitor.assets.dto.MwAddAndUpdateTangibleAssetsTable;
import cn.mw.monitor.assets.dto.MwGetAssetsTemplateInfoDTO;
import cn.mw.monitor.assets.service.MwAssetsInfoSyncService;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MwAssetsInfoSyncServiceImpl implements MwAssetsInfoSyncService {
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Resource
    private MwAssetsInfoSyncDao mwAssetsInfoSyncDao;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MwEngineCommonsService mwEngineCommonsService;

    @Autowired
    private MwTangibleAssetsServiceImpl mwTangibleAssetsServiceImpl;

    @Autowired
    private MWMessageService mwMessageService;
    @Value("${assets.push.enable}")
    private boolean assetsPush;

    @Override
    public Reply assetsInfoSync(String groupName) {
        Reply reply;
        int num = 0;
        StringBuilder templateNames = new StringBuilder();
        //获取数据库zabbixServer表中查出serverId
        List<Integer> zabbixServerIds = mwAssetsInfoSyncDao.getZabbixServerInfoIds();
        List<Map> listMap = new ArrayList<>();
        for (Integer id : zabbixServerIds) {
            //调用ZabbixAPI接口，获取hostid、hostName
            MWZabbixAPIResult hostInfoResult = mwtpServerAPI.getGroupHostByName(id, groupName);
            if(hostInfoResult != null && !hostInfoResult.isFail()){
                JsonNode data = (JsonNode) hostInfoResult.getData();
                if (data.size() > 0) {
                    for (JsonNode hostGroup : data) {
                        if (hostGroup.get("hosts").size() > 0) {
                            hostGroup.get("hosts").forEach(host -> {
                                Map map = new HashMap();
                                map.put("hostid", host.get("hostid").asText());
                                map.put("name", host.get("name").asText());
                                map.put("serverId", id);
                                listMap.add(map);
                            });
                        }
                    }
                }
            }
        }
        List<String> ids = new ArrayList<>();
        if (listMap != null && listMap.size() > 0) {
            for (Map p : listMap) {
                String hostId = p.get("hostid").toString();
                Integer serverId = Integer.valueOf(p.get("serverId").toString());
                String hostName = p.get("name").toString();
                MwAddAndUpdateTangibleAssetsTable tangibleAssetsTable = new MwAddAndUpdateTangibleAssetsTable();
                tangibleAssetsTable.setAssetsName(hostName);
                tangibleAssetsTable.setHostName(hostName);
                tangibleAssetsTable.setCreator(iLoginCacheInfo.getLoginName());
                tangibleAssetsTable.setModifier(iLoginCacheInfo.getLoginName());
                tangibleAssetsTable.setAssetsId(hostId);
                tangibleAssetsTable.setDeleteFlag(0);
                tangibleAssetsTable.setEnable("ACTIVE");
                tangibleAssetsTable.setMonitorFlag(1);
                tangibleAssetsTable.setSettingFlag(0);
                tangibleAssetsTable.setTpServerHostName(hostName);
                tangibleAssetsTable.setMonitorServerId(serverId);

                MWZabbixAPIResult hostInfoResult = mwtpServerAPI.getHostInfosById(serverId, hostId, hostName);
                JsonNode datas = (JsonNode) hostInfoResult.getData();
                String enginemanageId = "";
                String ip = "";
                if (datas != null && datas.size() > 0) {
                    JsonNode data = datas.get(0);
                    if (data.get("parentTemplates").size() > 0) {
                        //根据接口Api获取templateId
                        String templateId = data.get("parentTemplates").get(0).get("templateid").asText();
                        String templateName = data.get("parentTemplates").get(0).get("name").asText();
                        String assetsTemplateId = mwAssetsInfoSyncDao.getAssetsTemplateId(templateId, serverId);
                        tangibleAssetsTable.setInBandIp(ip);
                        if (data.get("interfaces").size() > 0) {
                            ip = data.get("interfaces").get(0).get("ip").asText();
                            tangibleAssetsTable.setInBandIp(ip);
                        }
                        tangibleAssetsTable.setTemplateId(templateId);
                        //如果数据库中获取不到assetsTemplateId，则忽略该条数据。
                        if (Strings.isNullOrEmpty(assetsTemplateId)) {
                            if (templateNames.toString().indexOf(templateName) == -1) {
                                templateNames.append(templateName);
                                templateNames.append("、");
                            }
                            num++;
                            continue;
                        }
                        List<String> assetIds = mwAssetsInfoSyncDao.countAssectNotHostId(tangibleAssetsTable);
                        //如果serverId、tpServerHostName、inBandIp全部相同，仅hostId不同，则先删除后新建
                        if (assetIds != null && assetIds.size() > 0) {
                            //删除
                            mwAssetsInfoSyncDao.deleteAssetsInfoByRepeat(assetIds);
                        }
                        //通过hostId、serverId、tpServerHostName、inBandIp全部相同，则为重复数据，忽略
                        Integer sum = mwAssetsInfoSyncDao.countAssectByHostId(tangibleAssetsTable);
                        if (sum != null && sum.intValue() == 0) {
                            //没有重复数据，新增插入
                            //根据proxyId查询enginemanageId
                            if (data.get("proxy_hostid") != null && !"0".equals(data.get("proxy_hostid").asText())) {
                                String proxyId = data.get("proxy_hostid").asText();
                                enginemanageId = mwAssetsInfoSyncDao.getEnginemanageId(proxyId, serverId);
                                if (!Strings.isNullOrEmpty(enginemanageId)) {
                                    mwEngineCommonsService.updateMonitorNums(true, enginemanageId, hostId);
                                }
                            }
                            tangibleAssetsTable.setPollingEngine(enginemanageId);
                            //依据assetsTemplateId，获取资源模板信息
                            MwGetAssetsTemplateInfoDTO assetsTemplateInfo = mwAssetsInfoSyncDao.getAssetsTemplateInfoById(assetsTemplateId);
                            tangibleAssetsTable.setAssetsTypeId(assetsTemplateInfo.getAssetsTypeId());
                            tangibleAssetsTable.setAssetsTypeSubId(assetsTemplateInfo.getSubAssetsTypeId());
                            tangibleAssetsTable.setMonitorMode(assetsTemplateInfo.getMonitorMode());
                            tangibleAssetsTable.setManufacturer(assetsTemplateInfo.getBrand());
                            tangibleAssetsTable.setSpecifications(assetsTemplateInfo.getSpecification());
                            tangibleAssetsTable.setDescription(assetsTemplateInfo.getDescription());
                            tangibleAssetsTable.setTemplateId(templateId);
                            tangibleAssetsTable.setId(UUIDUtils.getUUID());
                            mwAssetsInfoSyncDao.InsertAssetsInfo(tangibleAssetsTable);
                            ids.add(tangibleAssetsTable.getId());
                        }
                    }
                }
            }
        }
        if(assetsPush && CollectionUtils.isNotEmpty(ids)){
            //资产数据变更，kafka推送消息
            mwTangibleAssetsServiceImpl.tangibleAssetsPushConvert(ids);
        }
        if (num > 0) {
            String message = templateNames.toString();
            message = message.substring(0, message.length() - 1);
            return new Reply(200, "部分数据同步成功，模板：" + message + "不存在，已被忽略。");
        } else {
            return new Reply(200, "数据同步成功！");
        }
    }

    /**
     * 将猫维上的资产名称同步到zabbix的可见名称
     * @return
     */
    @Override
    public Reply syncAssetsNameReachZabbix(Integer type) {
        try {
            String tableName;
            if(type != null && type == 0){
                tableName = "mw_tangibleassets_table";
            }else{
                tableName = "mw_outbandassets_table";
            }
            //查询所有资产的主机ID和名称
            List<Map<String, Object>> assetsMaps = mwAssetsInfoSyncDao.selectAssetsIdAndName(tableName);
            if(CollectionUtils.isEmpty(assetsMaps))return Reply.ok("没有需要同步的数据");
            int successCount = 0;
            int errorCount = 0;
            for (Map<String, Object> assetsMap : assetsMaps) {
                Object assetsId = assetsMap.get("assetsId");
                Object assetsName = assetsMap.get("assetsName");
                Object serverId = assetsMap.get("serverId");
                if(assetsId == null || assetsName == null || serverId == null)continue;
                MWZabbixAPIResult result = mwtpServerAPI.hostUpdateSoName(Integer.parseInt(serverId.toString()),assetsId.toString(), assetsName.toString());
                if (result != null && !result.isFail()){
                    successCount++;
                    continue;
                }
                errorCount++;
            }
            String text;
            if(type != null && type == 0){
                text = "同步有形资产名称到zabbix可见名称完成，成功同步条数"+successCount+"，失败条数："+errorCount;
            }else{
                text = "同步带外资产名称到zabbix可见名称完成，成功同步条数"+successCount+"，失败条数："+errorCount;
            }
            log.info(text);
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            List<MWUser> mwUsers = new ArrayList<>();
            mwUsers.add(MWUser.builder().userId(userId).userName(iLoginCacheInfo.getLoginName()).build());
            mwMessageService.sendAssetsNameSyncSuccessMessage(text,mwUsers);
            return Reply.ok("资产名称同步成功");
        }catch (Throwable e){
            log.error("同步资产名称失败，失败信息{}",e);
            return Reply.fail("同步资产名称失败，失败信息:"+e.getMessage());
        }
    }
}
