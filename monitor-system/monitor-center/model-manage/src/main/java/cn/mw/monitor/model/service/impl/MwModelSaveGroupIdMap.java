package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Callable;


@Slf4j
@Transactional
public class MwModelSaveGroupIdMap implements Callable<Boolean> {

    private MWTPServerAPI mwtpServerAPI;

    private String typeName;

    private Integer assetsSubTypeId;

    private List<MwModelAssetsGroupTable> groupServerTables;

    public MwModelSaveGroupIdMap(MWTPServerAPI mwtpServerAPI, String typeName, Integer assetsSubTypeId, List<MwModelAssetsGroupTable> groupServerTables) {
        this.mwtpServerAPI = mwtpServerAPI;
        this.typeName = typeName;
        this.assetsSubTypeId = assetsSubTypeId;
        this.groupServerTables = groupServerTables;
    }

    @Override
    public Boolean call() {
        Boolean flag = false;
        try {
            if (typeName != null && !typeName.equals("")) {
                MWZabbixAPIResult mwZabbixAPIResult = mwtpServerAPI.hostGroupGet(mwtpServerAPI.getServerId(), "[分组]" + typeName, true);
                if (mwZabbixAPIResult.getCode() == 0) {
                    JsonNode node = (JsonNode) mwZabbixAPIResult.getData();
                    if (node.size() > 0) {
                        String groupid = "";
                        for (JsonNode data : node) {
                            groupid = data.get("groupid").asText();
                        }
                        MwModelAssetsGroupTable groupTable = new MwModelAssetsGroupTable();
                        groupTable.setGroupId(groupid);
                        groupTable.setMonitorServerId(mwtpServerAPI.getServerId());
                        groupTable.setAssetsSubtypeId(assetsSubTypeId);
                        groupServerTables.add(groupTable);
                    } else {
                        MWZabbixAPIResult resultData = mwtpServerAPI.hostgroupCreate(mwtpServerAPI.getServerId(), "[分组]" + typeName);
                        if (resultData.getCode() == 0) {
                            JsonNode data = (JsonNode) resultData.getData();
                            if (data.size() > 0) {
                                String groupid = "";
                                if (data.size() > 0) {
                                    JsonNode a2 = data.get("groupids");
                                    groupid = a2.get(0).asText();
                                }
                                MwModelAssetsGroupTable groupTable = new MwModelAssetsGroupTable();
                                groupTable.setGroupId(groupid);
                                groupTable.setMonitorServerId(mwtpServerAPI.getServerId());
                                groupTable.setAssetsSubtypeId(assetsSubTypeId);
                                groupServerTables.add(groupTable);
                            } else {
                                flag = true;
                                throw new Exception("创建主机群组失败：" + mwtpServerAPI.getServerId());
                            }
                        }

                    }
                }
            } else {
                //类型名称为空
            }
        } catch (Exception e) {
            log.error("fail to call, case by {}", e);
            flag = true;
        }
        return flag;
    }


}
