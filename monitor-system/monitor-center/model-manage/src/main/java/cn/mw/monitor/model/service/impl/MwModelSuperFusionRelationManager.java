package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.data.InstanceNotifyType;
import cn.mw.monitor.model.dto.SuperFusionInfo;
import cn.mw.monitor.model.dto.SuperFusionInstanceChangeParam;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.superfusion.MwQuerySuperFusionParam;
import cn.mw.monitor.model.param.superfusion.SuperFusionTreeParam;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.service.model.dto.InstanceLine;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.VirtualGroup;
import cn.mw.monitor.service.model.param.QueryRelationInstanceModelParam;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.model.param.MatchFusionModelTypeEnum.*;

/**
 * @author qzg
 * @date 2023/4/10
 */
@Service
@Slf4j
public class MwModelSuperFusionRelationManager {
    private int pageSize = 10000;
    @Autowired(required = false)
    private ConnectionPool connectionPool;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Autowired
    private MwModelViewService mwModelViewService;

    public void updateSuperFusionInfo(MwQuerySuperFusionParam param, List<SuperFusionInfo> list, InstanceNotifyType type) throws Exception {

        Map<String, List<SuperFusionInfo>> superFusionInfoMap = new HashMap<>();
        Map<String, SuperFusionInfo> dataMap = new HashMap<>();
        List<SuperFusionInfo> roots = new ArrayList<>();

        for (SuperFusionInfo superFusionInfo : list) {
            SuperFusionTreeParam superFusionDataInfoDTO = superFusionInfo.getDataInfoDTO();

            dataMap.put(superFusionDataInfoDTO.getId(), superFusionInfo);
            List<SuperFusionInfo> childs = superFusionInfoMap.get(superFusionDataInfoDTO.getPId());
            if (null == childs) {
                childs = new ArrayList<>();
                superFusionInfoMap.put(superFusionDataInfoDTO.getPId(), childs);
            }
            childs.add(superFusionInfo);
        }
        log.info("superFusion ->> dataMap" + dataMap);
        for (SuperFusionInfo superFusionInfo : list) {
            SuperFusionTreeParam superFusionDataInfoDTO = superFusionInfo.getDataInfoDTO();
            SuperFusionInfo superFusionInfo1 = dataMap.get(superFusionDataInfoDTO.getPId());
            if (null == superFusionInfo1) {
                roots.add(superFusionInfo);
            }
        }

        //遍历生成图形点和线
        List<VirtualGroup> virtualGroups = new ArrayList<>();
        for (SuperFusionInfo root : roots) {
            VirtualGroup virtualGroup = new VirtualGroup();
            Set<InstanceNode> nodes = new HashSet<>();
            List<InstanceLine> lines = new ArrayList<>();

            AddAndUpdateModelInstanceParam rootInstanceParam = root.getInstanceParam();
            NodeParam nodeParam = new NodeParam(rootInstanceParam.getModelId(), rootInstanceParam.getInstanceId());
            InstanceNode rootNode = new InstanceNode(nodeParam);
            virtualGroup.setRoot(rootNode);
            doVisiteVCenter(root, superFusionInfoMap, lines, nodes);
            virtualGroup.setLineList(lines);
            virtualGroup.setNodes(new ArrayList<>(nodes));
            virtualGroups.add(virtualGroup);
        }
        log.info("superFusion ->> virtualGroups" + virtualGroups);
        //更新图形数据库
        Session session = connectionPool.getSession();
        for (VirtualGroup virtualGroup : virtualGroups) {
            switch (type) {
                case VirtualSyncInit:
                case VirtualSyncIns:
                    ModelAssetUtils.addInstanceTopo(session, ModelAssetUtils.COMMON_SPACE + "_" + param.getModelId() + "_" + param.getModelInstanceId(), virtualGroup.getNodes(), virtualGroup.getLineList());
                    break;
                case VirtualSyncDelete:
                    for (InstanceNode node : virtualGroup.getNodes()) {
                        ModelAssetUtils.deleteInstanceDirectTopo(session, ModelAssetUtils.COMMON_SPACE + "_" + param.getModelId() + "_" + param.getModelInstanceId(), node);
                    }
            }
        }
    }

    private void doVisiteVCenter(SuperFusionInfo root, Map<String, List<SuperFusionInfo>> superFusionInfoMap
            , List<InstanceLine> lines, Set<InstanceNode> nodes) {
        AddAndUpdateModelInstanceParam startParam = root.getInstanceParam();
        NodeParam startNodeParam = new NodeParam(startParam.getModelId(), startParam.getInstanceId());
        InstanceNode startNode = new InstanceNode(startNodeParam);
        nodes.add(startNode);

        List<SuperFusionInfo> childs = superFusionInfoMap.get(root.getDataInfoDTO().getId());
        if (null != childs) {
            for (SuperFusionInfo superFusionInfo : childs) {
                AddAndUpdateModelInstanceParam endParam = superFusionInfo.getInstanceParam();
                NodeParam endNodeParam = new NodeParam(endParam.getModelId(), endParam.getInstanceId());
                InstanceNode endNode = new InstanceNode(endNodeParam);
                nodes.add(endNode);
                InstanceLine line = new InstanceLine(startNode, endNode);
                lines.add(line);
            }

            for (SuperFusionInfo child : childs) {
                doVisiteVCenter(child, superFusionInfoMap, lines, nodes);
            }
        }
    }

    public SuperFusionInstanceChangeParam compareSuperFusionInfo(Integer instanceId, List<SuperFusionTreeParam> list) throws Exception {
        SuperFusionInstanceChangeParam changeParam = new SuperFusionInstanceChangeParam();

        //获取es中已存在的数据
        List<SuperFusionTreeParam> oldDatas = getSuperFusionDataInfo(instanceId);
        //API获取的最新数据
        List<SuperFusionTreeParam> newDatas = list;


        Map<String, SuperFusionTreeParam> oldMap = oldDatas.stream().collect(Collectors.toMap(s -> s.getId(), s -> s, (
                value1, value2) -> {
            return value2;
        }));
        Map<String, SuperFusionTreeParam> newMap = newDatas.stream().collect(Collectors.toMap(s -> s.getId(), s -> s, (
                value1, value2) -> {
            return value2;
        }));

        List<SuperFusionTreeParam> deleteDataList = new ArrayList<>();
        List<SuperFusionTreeParam> AddDataList = new ArrayList<>();
        List<SuperFusionTreeParam> updateDataList = new ArrayList<>();
        List<SuperFusionTreeParam> pIdDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(oldDatas)) {
            changeParam.setHasSuperFusionData(true);
            //开始比对增删查改数据
            for (SuperFusionTreeParam oldData : oldDatas) {
                //判断是否有删除数据
                SuperFusionTreeParam deleteData = checkExistInMap(oldData, newMap);
                if (null == deleteData) {
                    deleteDataList.add(oldData);
                }
            }
            for (SuperFusionTreeParam newData : newDatas) {
                //判断是否有新增数据
                SuperFusionTreeParam addData = checkExistInMap(newData, oldMap);
                if (null == addData) {
                    if (!Strings.isNullOrEmpty(newData.getName())) {
                        AddDataList.add(newData);
                        //添加其父节点数据，在eno4j中可以连成网状结构
                        if (oldMap.containsKey(newData.getPId())) {
                            pIdDataList.add(oldMap.get(newData.getPId()));
                        }
                    }
                } else {
                    modelInstanceCopySetting(addData, newData);
                    updateDataList.add(newData);
                }
            }
        } else {
            changeParam.setHasSuperFusionData(false);
        }
        pIdDataList = pIdDataList.stream().distinct().collect(Collectors.toList());
        Map<String, SuperFusionTreeParam> parentMap = pIdDataList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s, (
                value1, value2) -> {
            return value2;
        }));
        //父节点数据中，只保留一个最上层根节点。
        Iterator<SuperFusionTreeParam> iterator = pIdDataList.iterator();
        while (iterator.hasNext()) {
            SuperFusionTreeParam parentInfo = iterator.next();
            if (parentMap.containsKey(parentInfo.getPId())) {
                iterator.remove();
            }
        }
        changeParam.setDeleteDatas(deleteDataList);
        changeParam.setAddDatas(AddDataList);
        changeParam.setUpdateDatas(updateDataList);
        changeParam.setPIdDatas(pIdDataList);
        return changeParam;
    }

    private void modelInstanceCopySetting(SuperFusionTreeParam oldData, SuperFusionTreeParam newData) {
        newData.setModelIndex(oldData.getModelIndex());
        newData.setModelInstanceId(oldData.getModelInstanceId());
        newData.setModelId(oldData.getModelId());
        newData.setInstanceName(oldData.getInstanceName());
    }

    private SuperFusionTreeParam checkExistInMap(SuperFusionTreeParam dataInfo, Map<String, SuperFusionTreeParam> dataMap) {
        SuperFusionTreeParam data = dataMap.get(dataInfo.getId());
        return data;
    }

    private List<SuperFusionTreeParam> getSuperFusionDataInfo(Integer instanceId) throws Exception {
        //根据Rancher获取关联模型属性
        List<Integer> modelIdList = Arrays.asList(SUPER_FUSION.getModelId(),FUSION_CLUSTER.getModelId(), FUSION_HOST.getModelId(), FUSION_VM.getModelId(), FUSION_STORAGE.getModelId());
        List<ModelInfo> modelInfos = mwModelManageDao.selectModelListByIds(modelIdList);
        QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
        param1.setRelationInstanceId(instanceId);
        List<String> modelIndexList = modelInfos.stream().map(ModelInfo::getModelIndex).collect(Collectors.toList());
        param1.setModelIndexs(modelIndexList);
        List<Map<String, Object>> listMap = mwModelViewService.selectInstanceInfoByRelationInstanceId(param1);
        List<SuperFusionTreeParam> ret = MwModelUtils.convertEsData(SuperFusionTreeParam.class, listMap);
        return ret;
    }

}
