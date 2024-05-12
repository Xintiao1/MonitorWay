package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.service.model.dto.InstanceLine;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.model.data.InstanceNotifyType;
import cn.mw.monitor.service.model.dto.VirtualGroup;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.citrix.MwModelCitrixInfoParam;
import cn.mw.monitor.model.param.citrix.MwModelCitrixRelationParam;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author qzg
 * @date 2023/4/10
 */
@Service
@Slf4j
public class MwModelCitrixRelationManager {
    @Autowired
    private MwModelViewServiceImpl mwModelViewServiceImpl;
    private int pageSize = 10000;
    @Autowired(required = false)
    private ConnectionPool connectionPool;

    public void compareCitrixInfo(List<String> modelIndexs, List<Integer> InstanceIds, List<AddAndUpdateModelInstanceParam> instanceInfoList) {
        QueryInstanceModelParam instanceModelParam = new QueryInstanceModelParam();
        instanceModelParam.setModelIndexs(modelIndexs);
        instanceModelParam.setInstanceIds(InstanceIds);
        instanceModelParam.setPageSize(pageSize);
        //获取es中已存在的数据
        Map<String, Object> mapInfo = mwModelViewServiceImpl.getModelListInfoByBase(instanceModelParam);
        List<MwModelCitrixInfoParam> citrixList = new ArrayList<>();
        if (mapInfo != null && mapInfo.get("data") != null) {
            citrixList = JSON.parseArray(JSONObject.toJSONString(mapInfo.get("data")), MwModelCitrixInfoParam.class);
        }
        List<AddAndUpdateModelInstanceParam> newInsertList = instanceInfoList;
        List<MwModelCitrixInfoParam> oldDataList = citrixList;
        // 将model_id+instanceName 为key，来判断数据是否重复。
        Map<String, AddAndUpdateModelInstanceParam> newMap = newInsertList.stream().collect(Collectors.toMap(s -> s.getModelId() + "_" + s.getInstanceName(), s -> s));
        Map<String, MwModelCitrixInfoParam> oldMap = oldDataList.stream().collect(Collectors.toMap(s -> s.getModelId() + "_" + s.getInstanceName(), s -> s));
        //比较获取新增删除修改的数据
        //开始比对增删查改数据
        List<MwModelCitrixInfoParam> deleteList = new ArrayList<>();
        for (MwModelCitrixInfoParam oldData : oldDataList) {
            //判断是否有删除数据
            //旧的数据在newMap中不存在，则为要删除的数据
            MwModelCitrixInfoParam existData = checkExistDeleteInMap(oldData, newMap);
            if (null == existData) {
                continue;
            } else {
                deleteList.add(existData);
            }

            //判断数据是否变更
//            if(!oldData.isSame(newData)){
//                changeContext.addUpdate(oldData ,cacheInfoMap);
//            }
        }

        for (AddAndUpdateModelInstanceParam newData : newInsertList) {
            //判断是否有新增数据
            AddAndUpdateModelInstanceParam oldData = checkExistAddInMap(newData, oldMap);

//            if(null == oldData){
//                changeContext.addAdd(newData ,cacheInfoMap);
//            }
        }


    }

    public void doVisiteCitrixInfo(Integer rootInstanceId,List<AddAndUpdateModelInstanceParam> instanceInfoList,Map<String, Map<String, List<MwModelCitrixRelationParam>>> mwCitrixRelationMap,
                                   InstanceNotifyType type,List<VirtualGroup> virtualGroups) throws Exception {
        //遍历生成图形点和线
        for (AddAndUpdateModelInstanceParam instanceInfo : instanceInfoList) {
            VirtualGroup virtualGroup = new VirtualGroup();
            Set<InstanceNode> nodes = new HashSet<>();
            List<InstanceLine> lines = new ArrayList<>();
            Map<String, List<MwModelCitrixRelationParam>> listMap = mwCitrixRelationMap.get(instanceInfo.getModelIndex());
            NodeParam startNodeParam = new NodeParam(instanceInfo.getModelId(), instanceInfo.getInstanceId());
            InstanceNode startNode = new InstanceNode(startNodeParam);
            nodes.add(startNode);
            if (listMap != null && listMap.size() > 0) {
                List<MwModelCitrixRelationParam> citrixRelationList = listMap.get(instanceInfo.getInstanceName());
                if(CollectionUtils.isNotEmpty(citrixRelationList)){
                    for (MwModelCitrixRelationParam param : citrixRelationList) {
                        if(param!=null){
                            NodeParam endNodeParam = new NodeParam(param.getServiceNameModelId(), param.getServiceNameInstanceId());
                            InstanceNode endNode = new InstanceNode(endNodeParam);
                            nodes.add(endNode);
                            InstanceLine line = new InstanceLine(startNode, endNode);
                            lines.add(line);

                        }
                    }

                }
            } else {
                continue;
            }
            virtualGroup.setLineList(lines);
            virtualGroup.setNodes(new ArrayList<>(nodes));
            virtualGroups.add(virtualGroup);
        }
        //更新图形数据库
        Session session = connectionPool.getSession();
        for (VirtualGroup virtualGroup : virtualGroups) {
            switch (type) {
                case VirtualSyncInit:
                case VirtualSyncIns:
                    ModelAssetUtils.addInstanceTopo(session, ModelAssetUtils.CITRIX_SPACE+rootInstanceId, virtualGroup.getNodes(), virtualGroup.getLineList());
                    break;
                case VirtualSyncDelete:
                    for (InstanceNode node : virtualGroup.getNodes()) {
                        ModelAssetUtils.deleteInstanceDirectTopo(session, ModelAssetUtils.CITRIX_SPACE+rootInstanceId, node);
                    }
            }
        }
    }

    public void deleteInstanceTopo(Integer modelInstanceId) throws Exception {
        Session session = connectionPool.getSession();
        ModelAssetUtils.deleteInstanceTopo(session, ModelAssetUtils.CITRIX_SPACE+modelInstanceId);
    }



    private MwModelCitrixInfoParam checkExistDeleteInMap(MwModelCitrixInfoParam data, Map<String, AddAndUpdateModelInstanceParam> map) {
        AddAndUpdateModelInstanceParam existData = map.get(data.getModelId() + "_" + data.getInstanceName());
        if (existData == null) {
            return data;
        }
        return null;
    }

    private AddAndUpdateModelInstanceParam checkExistAddInMap(AddAndUpdateModelInstanceParam data, Map<String, MwModelCitrixInfoParam> map) {
        MwModelCitrixInfoParam existData = map.get(data.getModelId() + "_" + data.getInstanceName());
        if (existData == null) {
            return data;
        }
        return null;
    }
}
