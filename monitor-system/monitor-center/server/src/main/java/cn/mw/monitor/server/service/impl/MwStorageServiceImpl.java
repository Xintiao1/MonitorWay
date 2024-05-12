package cn.mw.monitor.server.service.impl;

import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.service.alert.dto.ItemData;
import cn.mw.monitor.server.service.MwStorageService;
import cn.mw.monitor.service.server.api.dto.DiskListDto;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2020/12/17 10:07
 * @Version 1.0
 */
@Service
@Slf4j
public class MwStorageServiceImpl implements MwStorageService {


    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwServerManager mwServerManager;

    @Override
    public Reply getStorageVolInfo(int monitorServerId, String hostId, String typeName) {
        DiskListDto storageVolInfo = new DiskListDto();
        storageVolInfo.setUpdateTime(DateUtils.now());
        storageVolInfo.setType(typeName);
        List<String> itemNames = new ArrayList<>();
        ZabbixItemConstant.ITEMNAME_STORAGEVOL.forEach(item -> {
            itemNames.add("[" + typeName + "]" + item);
        });
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(monitorServerId, itemNames, hostId);
        if (!result.isFail()) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() > 0) {
                Double total = 0.0;
                Double used = 0.0;
                for (JsonNode node : jsonNode) {
                    String name = node.get("name").asText();
                    String lastValue = node.get("lastvalue").asText();
                    String units = node.get("units").asText();
                    if (name.indexOf(ZabbixItemConstant.ITEMNAME_STORAGEVOL.get(0)) != -1) {
                        storageVolInfo.setDiskTotal(UnitsUtil.getValueWithUnits(lastValue, units));
                        total = Double.parseDouble(lastValue);
                    } else if (name.indexOf(ZabbixItemConstant.ITEMNAME_STORAGEVOL.get(1)) != -1) {
                        storageVolInfo.setDiskUser(UnitsUtil.getValueWithUnits(lastValue, units));
                        used = Double.parseDouble(lastValue);
                    }
                    Double free = total - used;
                    Double userRate = 0.0;
                    if (total != 0.0) {
                        userRate = (used / total) * 100;
                    }
                    storageVolInfo.setDiskUserRate(UnitsUtil.getValueWithUnits(userRate.toString(), "%"));
                    storageVolInfo.setDiskFree(UnitsUtil.getValueWithUnits(free.toString(), units));
                }
            }
        }
        return Reply.ok(storageVolInfo);
    }

    @Override
    public Reply getItemNameLikes(int monitorServerId, String hostId, String itemName) {
        List<ItemData> list = new ArrayList<>();
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(monitorServerId, itemName, hostId, false);
        if (!result.isFail()) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    ItemData itemData = new ItemData();
                    String name= node.get("name").asText();
                    String chName = mwServerManager.getChName(node.get("name").asText());
                    if (chName.indexOf("[") != -1) {
                        itemData.setName(name.substring(name.indexOf("]") + 1));
                        itemData.setChName(chName.substring(chName.indexOf("]") + 1));
                    } else {
                        itemData.setName(name);
                        itemData.setChName(chName);
                    }
                    list.add(itemData);
                }
            }
        }
        List<ItemData> collect = list.stream().distinct().collect(Collectors.toList());
        return Reply.ok(collect);
    }

}
