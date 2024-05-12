package cn.mw.monitor.server.service.impl;

import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.service.alert.dto.ItemData;
import cn.mw.monitor.server.service.MwDatabaseService;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author syt
 * @Date 2020/12/10 10:39
 * @Version 1.0
 */
@Service
@Slf4j
public class MwDatabaseServiceImpl implements MwDatabaseService {
    private static final Logger logger = LoggerFactory.getLogger("MwDatabaseService");


    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MwServerManager mwServerManager;

    @Override
    public Reply getRelevantInfoForTable(int monitorServerId, String hostId, String itemName) {
        return getItemsIsFilter(monitorServerId, hostId, null, itemName, false);
    }

    @Override
    public Reply getSelectInfoForTable(int monitorServerId, String hostId) {
        List<ItemData> list = new ArrayList<>();
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(monitorServerId, ZabbixItemConstant.ITEMNAME_SELECT, hostId);
        if (!result.isFail()) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    ItemData item = new ItemData();
                    item.setName(node.get("name").asText());
                    //查找中文名称
                    item.setChName(mwServerManager.getChName(node.get("name").asText()));
                    String lastValue = node.get("lastvalue").asText();
                    Double newValue = Double.parseDouble(UnitsUtil.getValueWithUnits(lastValue, node.get("units").asText()));
                    item.setNewValue(newValue);
                    item.setValue(lastValue);
                    list.add(item);
                }
            }
        }
        return Reply.ok(list);
    }

    @Override
    public Reply getRelevantInfoForTable(int monitorServerId, String hostId, List<String> itemNames) {
        return getItemsIsFilter(monitorServerId, hostId, itemNames, null, true);
    }

    /**
     * 根据名称精准查询或模糊查询监控项相关最新数据
     *
     * @param monitorServerId
     * @param hostId
     * @param itemNames
     * @param itemName
     * @param flag
     * @return
     */
    private Reply getItemsIsFilter(int monitorServerId, String hostId, List<String> itemNames, String itemName, Boolean flag) {
        List<ItemApplication> list = new ArrayList<>();
        MWZabbixAPIResult result;
        if (flag) {
            result = mwtpServerAPI.itemGetbyFilter(monitorServerId, itemNames, hostId);
        } else {
            result = mwtpServerAPI.itemGetbyType(monitorServerId, itemName, hostId, false);
        }
        if (!result.isFail()) {
            String data = String.valueOf(result.getData());
            list = JSONArray.parseArray(data, ItemApplication.class);
            for (ItemApplication item : list) {
                //查找中文名称
                item.setChName(mwServerManager.getChName(item.getName()));
                if (item.getName() != null && item.getName().indexOf("[") != -1) {
                    item.setTypeName(item.getName().substring(item.getName().indexOf("[") + 1, item.getName().indexOf("]")));
                }
                String newValue = "";
                if (!"0".equals(item.getValuemapid())) {
                    newValue = mwServerManager.getValueMapById(monitorServerId, item.getValuemapid(), item.getLastvalue());
                } else {
                    if ("uptime".equals(item.getUnits())) {
                        double v = Double.parseDouble(item.getLastvalue());
                        long l = new Double(v).longValue();
                        newValue = SeverityUtils.getLastTime(l);
                    } else {
                        if ("0".equals(item.getValue_type()) || "3".equals(item.getValue_type())) {
                            newValue = UnitsUtil.getValueWithUnits(item.getLastvalue(), item.getUnits());
                        } else {
                            newValue = item.getLastvalue();
                        }
                    }
                }
                item.setLastvalue(newValue);
            }
        }
        return Reply.ok(list);
    }
}
