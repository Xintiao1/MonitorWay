package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.es.action.EsDataOperation;
import cn.mw.monitor.es.action.EsIndexOperation;
import cn.mw.monitor.es.action.EsQueryOperation;
import cn.mw.monitor.es.action.EsUtil;
import cn.mw.monitor.model.data.InstanceNotifyType;
import cn.mw.monitor.model.dto.VCenterInfo;
import cn.mw.monitor.model.dto.VirtualCacheData;
import cn.mw.monitor.model.dto.VirtualInstanceChangeContext;
import cn.mw.monitor.model.dto.VirtualizationDataCacheInfo;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.ConnectCheckModelEnum;
import cn.mw.monitor.model.param.DeleteModelInstanceParam;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.service.model.dto.InstanceLine;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.model.dto.ModelVirtualDeleteContext;
import cn.mw.monitor.service.model.dto.VirtualGroup;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mw.monitor.service.virtual.dto.VirtualizationDataInfo;
import cn.mw.monitor.service.virtual.dto.VirtualizationType;
import cn.mwpaas.common.enums.DateUnitEnum;
import cn.mwpaas.common.utils.DateUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MwModelVirtualRelationManager implements InitializingBean {

    private boolean debug;

    private static final String VIRTUAL_INDEX = "virtual_device_cache";

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired(required = false)
    private ConnectionPool connectionPool;

    @Autowired
    private MwModelInstanceService mwModelInstanceService;

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void updateVCenter(List<VCenterInfo> list, InstanceNotifyType type) throws Exception {

        Map<String, List<VCenterInfo>> vCenterInfoMap = new HashMap<>();
        Map<String, VCenterInfo> dataMap = new HashMap<>();
        List<VCenterInfo> roots = new ArrayList<>();

        for (VCenterInfo vCenterInfo : list) {
            VirtualizationDataInfo virtualizationDataInfo = vCenterInfo.getVirtualizationDataInfo();

            dataMap.put(virtualizationDataInfo.getId(), vCenterInfo);
            List<VCenterInfo> childs = vCenterInfoMap.get(virtualizationDataInfo.getPId());
            if (null == childs) {
                childs = new ArrayList<>();
                vCenterInfoMap.put(virtualizationDataInfo.getPId(), childs);
            }
            childs.add(vCenterInfo);
        }

        for (VCenterInfo vCenterInfo : list) {
            VirtualizationDataInfo virtualizationDataInfo = vCenterInfo.getVirtualizationDataInfo();
            VCenterInfo vCenterInfo1 = dataMap.get(virtualizationDataInfo.getPId());
            if (null == vCenterInfo1) {
                roots.add(vCenterInfo);
            }
        }

        //遍历生成图形点和线
        List<VirtualGroup> virtualGroups = new ArrayList<>();
        for (VCenterInfo root : roots) {
            VirtualGroup virtualGroup = new VirtualGroup();
            Set<InstanceNode> nodes = new HashSet<>();
            List<InstanceLine> lines = new ArrayList<>();

            AddAndUpdateModelInstanceParam rootInstanceParam = root.getInstanceParam();
            NodeParam nodeParam = new NodeParam(rootInstanceParam.getModelId(), rootInstanceParam.getInstanceId());
            InstanceNode rootNode = new InstanceNode(nodeParam);
            virtualGroup.setRoot(rootNode);
            doVisiteVCenter(root, vCenterInfoMap, lines, nodes);
            virtualGroup.setLineList(lines);
            virtualGroup.setNodes(new ArrayList<>(nodes));
            virtualGroups.add(virtualGroup);
        }

        //更新图形数据库
        Session session = connectionPool.getSession();

        switch (type) {
            case VirtualSyncInit:
            case VirtualSyncIns:
                Date start = new Date();
                ModelAssetUtils.batchAddInstanceTopo(session, ModelAssetUtils.VIRTUAL_SPACE, virtualGroups);
                long interval = DateUtils.between(start, new Date(), DateUnitEnum.SECOND);
                log.info("end batchAddInstanceTopo interval:{} s", interval);
                break;
            case VirtualSyncDelete:
                Date start2 = new Date();
                ModelAssetUtils.batchDeleteInstanceDirectTopo(session, ModelAssetUtils.VIRTUAL_SPACE, virtualGroups);
                long interval2 = DateUtils.between(start2, new Date(), DateUnitEnum.SECOND);
                log.info("end batchDeleteInstanceDirectTopo interval:{} s", interval2);
        }

//        Date start1 = new Date();
//        for (VirtualGroup virtualGroup : virtualGroups) {
//            switch (type) {
//                case VirtualSyncInit:
//                case VirtualSyncIns:
//                    ModelAssetUtils.addInstanceTopo(session, ModelAssetUtils.VIRTUAL_SPACE, virtualGroup.getNodes(), virtualGroup.getLineList());
//                    break;
//                case VirtualSyncDelete:
//                    for (InstanceNode node : virtualGroup.getNodes()) {
//                        ModelAssetUtils.deleteInstanceDirectTopo(session, ModelAssetUtils.VIRTUAL_SPACE, node);
//                    }
//            }
//        }
//        long interval1 = DateUtils.between(start1, new Date(), DateUnitEnum.SECOND);
//        log.info("end for::addOrDeleteInstanceTopo interval:{} s", interval1);

        if (debug) {
            log.info(JSON.toJSONString(virtualGroups));
        }

    }

    private void doVisiteVCenter(VCenterInfo root, Map<String, List<VCenterInfo>> vCenterInfoMap
            , List<InstanceLine> lines, Set<InstanceNode> nodes) {
        AddAndUpdateModelInstanceParam startParam = root.getInstanceParam();
        NodeParam startNodeParam = new NodeParam(startParam.getModelId(), startParam.getInstanceId());
        InstanceNode startNode = new InstanceNode(startNodeParam);
        nodes.add(startNode);

        List<VCenterInfo> childs = vCenterInfoMap.get(root.getVirtualizationDataInfo().getId());
        if (null != childs) {
            for (VCenterInfo vCenterInfo : childs) {
                AddAndUpdateModelInstanceParam endParam = vCenterInfo.getInstanceParam();
                NodeParam endNodeParam = new NodeParam(endParam.getModelId(), endParam.getInstanceId());
                InstanceNode endNode = new InstanceNode(endNodeParam);
                nodes.add(endNode);
                InstanceLine line = new InstanceLine(startNode, endNode);
                lines.add(line);
            }

            for (VCenterInfo child : childs) {
                doVisiteVCenter(child, vCenterInfoMap, lines, nodes);
            }
        }
    }

    public VirtualInstanceChangeContext compareVMWareInfo(Integer instanceId, List<VirtualizationDataInfo> list) throws Exception {
        VirtualInstanceChangeContext changeContext = new VirtualInstanceChangeContext();
        QueryInstanceModelParam esParam = new QueryInstanceModelParam();
        List<String> indexes = new ArrayList<>();
        indexes.add(VIRTUAL_INDEX);
        esParam.setModelIndexs(indexes);

        VirtualCacheData oldCacheData = getVirtualCacheData(instanceId);
        if (null != oldCacheData) {
            changeContext.setHasVirtualData(true);
            List<VirtualizationDataCacheInfo> oldCacheInfo = JSON.parseArray(oldCacheData.getData(), VirtualizationDataCacheInfo.class);
            for (VirtualizationDataCacheInfo cacheInfo : oldCacheInfo) {
                cacheInfo.initKey();
            }
            changeContext.setOriData(oldCacheInfo);
            List<VirtualizationDataInfo> oldInfo = oldCacheInfo.stream().map(VirtualizationDataCacheInfo::getVirtualizationDataInfo).collect(Collectors.toList());

            //构造缓存类型map,在比对差异后,需要进行转换
            Map<String, VirtualizationDataCacheInfo> cacheInfoMap = new HashMap<>();
            for (VirtualizationDataCacheInfo cacheInfo : oldCacheInfo) {
                VirtualizationDataInfo virtualizationDataInfo = cacheInfo.getVirtualizationDataInfo();
                cacheInfoMap.put(virtualizationDataInfo.getId(), cacheInfo);
                cacheInfoMap.put(virtualizationDataInfo.getUUID(), cacheInfo);
            }

            //使用api返回结果设置map
            Map<String, VirtualizationDataInfo> newMap = new HashMap<>();
            fillCompareMap(newMap, list);

            //使用cache数据设置旧的map
            Map<String, VirtualizationDataInfo> oldMap = new HashMap<>();
            fillCompareMap(oldMap, oldInfo);

            //开始比对增删查改数据
            for (VirtualizationDataInfo oldData : oldInfo) {
                if (oldData.getType().equals(VirtualizationType.VCENTER.getType())) {
                    continue;
                }
                //判断是否有删除数据
                VirtualizationDataInfo newData = checkExistInMap(oldData, newMap);
                if (null == newData) {
                    changeContext.addDelete(oldData, cacheInfoMap);
                    continue;
                }

                //判断数据是否变更
                if (!oldData.isSame(newData)) {
                    changeContext.addUpdate(oldData, cacheInfoMap);
                }
            }

            for (VirtualizationDataInfo newData : list) {
                //判断是否有新增数据
                VirtualizationDataInfo oldData = checkExistInMap(newData, oldMap);

                if (null == oldData) {
                    changeContext.addAdd(newData, cacheInfoMap);
                }
            }
        } else {
            changeContext.setHasVirtualData(false);
        }
        return changeContext;
    }

    private VirtualizationDataInfo checkExistInMap(VirtualizationDataInfo data, Map<String, VirtualizationDataInfo> map) {
        VirtualizationDataInfo newData = map.get(data.getUUID());
        if (null == newData) {
            newData = map.get(data.getId());
        }

        return newData;
    }

    private void fillCompareMap(Map<String, VirtualizationDataInfo> map, List<VirtualizationDataInfo> list) {
        for (VirtualizationDataInfo oldData : list) {
            map.put(oldData.getId(), oldData);
            map.put(oldData.getUUID(), oldData);
        }
    }

    public boolean updateVirtualCacheData(VirtualCacheData virtualCacheData, InstanceNotifyType type) throws Exception {
        EsDataOperation esDataOperation = new EsDataOperation(restHighLevelClient);
        Map<String, Method> methodMap = EsUtil.getMethodMap(VirtualCacheData.class);
        Map dataMap = EsUtil.transformData(virtualCacheData, methodMap);

        if (type == InstanceNotifyType.VirtualSyncInit) {
            return esDataOperation.insert(VIRTUAL_INDEX, dataMap);
        }
        return esDataOperation.update(VIRTUAL_INDEX, dataMap);
    }

    public VirtualCacheData getVirtualCacheData(Integer intanceId) throws Exception {
        EsQueryOperation esQueryOperation = new EsQueryOperation(restHighLevelClient);
        Map<String, Object> esData = esQueryOperation.getOne(VIRTUAL_INDEX, intanceId.toString());
        if (null != esData && esData.size() > 0) {
            List<Map<String, Object>> listMap = new ArrayList<>();
            listMap.add(esData);
            List<VirtualCacheData> ret = MwModelUtils.convertEsData(VirtualCacheData.class, listMap);
            VirtualCacheData virtualCacheData = ret.get(0);
            virtualCacheData.setId(intanceId.toString());
            return virtualCacheData;
        }
        return null;
    }

    public ModelVirtualDeleteContext deleteVCenter(Integer instanceId) {
        try {
            ModelVirtualDeleteContext modelVirtualDeleteContext = new ModelVirtualDeleteContext();
            Session session = connectionPool.getSession();
            //获取es缓存信息
            VirtualCacheData cacheData = getVirtualCacheData(instanceId);
            List<VirtualizationDataCacheInfo> oldCacheInfo = JSON.parseArray(cacheData.getData(), VirtualizationDataCacheInfo.class);
            for (VirtualizationDataCacheInfo cacheInfo : oldCacheInfo) {
                cacheInfo.initKey();
            }

            NodeParam nodeParam = new NodeParam(ConnectCheckModelEnum.VCENTER.getModelId(), instanceId);
            InstanceNode instanceNode = new InstanceNode(nodeParam);

            //删除所有图节点
            ModelAssetUtils.deleteNodeAndLine(session, ModelAssetUtils.VIRTUAL_SPACE, instanceNode);

            //删除所有实例
            DeleteModelInstanceParam param = new DeleteModelInstanceParam();
            List<String> modelIndexes = oldCacheInfo.stream().map(VirtualizationDataCacheInfo::getModelIndex).collect(Collectors.toList());
            List<Integer> intanceIds = oldCacheInfo.stream().map(VirtualizationDataCacheInfo::getIntanceId).collect(Collectors.toList());

            param.setModelIndexs(modelIndexes);
            param.setInstanceIds(intanceIds);
            mwModelInstanceService.batchDeleteInstanceInfo(param);

            //删除es缓存
            EsDataOperation esDataOperation = new EsDataOperation(restHighLevelClient);
            esDataOperation.delete(VIRTUAL_INDEX, instanceId.toString());

            modelVirtualDeleteContext.setInstanceIds(intanceIds);
            return modelVirtualDeleteContext;
        } catch (Exception e) {
            log.error("deleteVCenter", e);
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //检查es索引是否创建
        EsIndexOperation esIndexOperation = new EsIndexOperation(restHighLevelClient);
        boolean isExist = esIndexOperation.checkIndex(VIRTUAL_INDEX);
        if (!isExist) {
            Map<String, Object> columnMap = EsUtil.getColumnMap(VirtualCacheData.class);
            esIndexOperation.createIndex(VIRTUAL_INDEX, columnMap);
        }
    }
}
