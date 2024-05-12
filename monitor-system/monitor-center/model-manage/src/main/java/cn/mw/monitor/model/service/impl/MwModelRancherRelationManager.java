package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.service.model.dto.InstanceLine;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.data.InstanceNotifyType;
import cn.mw.monitor.model.dto.RancherInfo;
import cn.mw.monitor.model.dto.RancherInstanceChangeParam;
import cn.mw.monitor.service.model.dto.VirtualGroup;
import cn.mw.monitor.service.model.dto.rancher.MwModelRancherDataInfoDTO;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.citrix.MwModelCitrixInfoParam;
import cn.mw.monitor.model.param.citrix.MwModelCitrixRelationParam;
import cn.mw.monitor.model.param.rancher.RancherInstanceParam;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.model.dto.ModelInfo;
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

import static cn.mw.monitor.model.param.MatchModelTypeEnum.*;

/**
 * @author qzg
 * @date 2023/4/10
 */
@Service
@Slf4j
public class MwModelRancherRelationManager {
    @Autowired
    private MwModelViewServiceImpl mwModelViewServiceImpl;
    private int pageSize = 10000;
    @Autowired(required = false)
    private ConnectionPool connectionPool;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Autowired
    private MwModelViewService mwModelViewService;

    public void updateRancherInfo(RancherInstanceParam param, List<RancherInfo> list, InstanceNotifyType type) throws Exception {

        Map<String, List<RancherInfo>> rancherInfoMap = new HashMap<>();
        Map<String, RancherInfo> dataMap = new HashMap<>();
        List<RancherInfo> roots = new ArrayList<>();

        for (RancherInfo rancherInfo : list) {
            MwModelRancherDataInfoDTO rancherDataInfoDTO = rancherInfo.getRancherDataInfoDTO();

            dataMap.put(rancherDataInfoDTO.getId(), rancherInfo);
            List<RancherInfo> childs = rancherInfoMap.get(rancherDataInfoDTO.getPId());
            if (null == childs) {
                childs = new ArrayList<>();
                rancherInfoMap.put(rancherDataInfoDTO.getPId(), childs);
            }
            childs.add(rancherInfo);
        }
        log.info("rancher ->> dataMap" + dataMap);
        for (RancherInfo rancherInfo : list) {
            MwModelRancherDataInfoDTO rancherDataInfoDTO = rancherInfo.getRancherDataInfoDTO();
            RancherInfo rancherInfo1 = dataMap.get(rancherDataInfoDTO.getPId());
            if (null == rancherInfo1) {
                roots.add(rancherInfo);
            }
        }

        //遍历生成图形点和线
        List<VirtualGroup> virtualGroups = new ArrayList<>();
        for (RancherInfo root : roots) {
            VirtualGroup virtualGroup = new VirtualGroup();
            Set<InstanceNode> nodes = new HashSet<>();
            List<InstanceLine> lines = new ArrayList<>();

            AddAndUpdateModelInstanceParam rootInstanceParam = root.getInstanceParam();
            NodeParam nodeParam = new NodeParam(rootInstanceParam.getModelId(), rootInstanceParam.getInstanceId());
            InstanceNode rootNode = new InstanceNode(nodeParam);
            virtualGroup.setRoot(rootNode);
            doVisiteVCenter(root, rancherInfoMap, lines, nodes);
            virtualGroup.setLineList(lines);
            virtualGroup.setNodes(new ArrayList<>(nodes));
            virtualGroups.add(virtualGroup);
        }
        log.info("rancher ->> virtualGroups" + virtualGroups);
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

    private void doVisiteVCenter(RancherInfo root, Map<String, List<RancherInfo>> rancherInfoMap
            , List<InstanceLine> lines, Set<InstanceNode> nodes) {
        AddAndUpdateModelInstanceParam startParam = root.getInstanceParam();
        NodeParam startNodeParam = new NodeParam(startParam.getModelId(), startParam.getInstanceId());
        InstanceNode startNode = new InstanceNode(startNodeParam);
        nodes.add(startNode);

        List<RancherInfo> childs = rancherInfoMap.get(root.getRancherDataInfoDTO().getId());
        if (null != childs) {
            for (RancherInfo rancherInfo : childs) {
                AddAndUpdateModelInstanceParam endParam = rancherInfo.getInstanceParam();
                NodeParam endNodeParam = new NodeParam(endParam.getModelId(), endParam.getInstanceId());
                InstanceNode endNode = new InstanceNode(endNodeParam);
                nodes.add(endNode);
                InstanceLine line = new InstanceLine(startNode, endNode);
                lines.add(line);
            }

            for (RancherInfo child : childs) {
                doVisiteVCenter(child, rancherInfoMap, lines, nodes);
            }
        }
    }


    public void doVisiteRancherInfo(Integer rootInstanceId, List<AddAndUpdateModelInstanceParam> instanceInfoList, Map<String, Map<String, List<MwModelCitrixRelationParam>>> mwCitrixRelationMap,
                                    InstanceNotifyType type, List<VirtualGroup> virtualGroups) throws Exception {
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
                if (CollectionUtils.isNotEmpty(citrixRelationList)) {
                    for (MwModelCitrixRelationParam param : citrixRelationList) {
                        if (param != null) {
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
                    ModelAssetUtils.addInstanceTopo(session, ModelAssetUtils.CITRIX_SPACE + rootInstanceId, virtualGroup.getNodes(), virtualGroup.getLineList());
                    break;
                case VirtualSyncDelete:
                    for (InstanceNode node : virtualGroup.getNodes()) {
                        ModelAssetUtils.deleteInstanceDirectTopo(session, ModelAssetUtils.CITRIX_SPACE + rootInstanceId, node);
                    }
            }
        }
    }

    public void deleteInstanceTopo(RancherInstanceParam param) throws Exception {
        Session session = connectionPool.getSession();
        ModelAssetUtils.deleteInstanceTopo(session, ModelAssetUtils.COMMON_SPACE + "_" + param.getModelId() + "_" + param.getModelInstanceId());
        log.info("rancher 实例拓扑数据删除成功");
    }

    public RancherInstanceChangeParam compareRancherInfo(Integer instanceId, List<MwModelRancherDataInfoDTO> list) throws Exception {
        RancherInstanceChangeParam changeParam = new RancherInstanceChangeParam();

        //获取es中已存在的数据
        List<MwModelRancherDataInfoDTO> oldDatas = getRancherDataInfo(instanceId);
        //API获取的最新数据
        List<MwModelRancherDataInfoDTO> newDatas = list;


        Map<String, MwModelRancherDataInfoDTO> oldMap = oldDatas.stream().collect(Collectors.toMap(s -> s.getId(), s -> s, (
                value1, value2) -> {
            return value2;
        }));
        Map<String, MwModelRancherDataInfoDTO> newMap = newDatas.stream().collect(Collectors.toMap(s -> s.getId(), s -> s, (
                value1, value2) -> {
            return value2;
        }));

        List<MwModelRancherDataInfoDTO> deleteDataList = new ArrayList<>();
        List<MwModelRancherDataInfoDTO> AddDataList = new ArrayList<>();
        List<MwModelRancherDataInfoDTO> updateDataList = new ArrayList<>();
        List<MwModelRancherDataInfoDTO> pIdDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(oldDatas)) {
            changeParam.setHasRancherData(true);
            //开始比对增删查改数据
            for (MwModelRancherDataInfoDTO oldData : oldDatas) {
                //判断是否有删除数据
                MwModelRancherDataInfoDTO deleteData = checkExistInMap(oldData, newMap);
                if (null == deleteData) {
                    deleteDataList.add(oldData);
                }
            }
            for (MwModelRancherDataInfoDTO newData : newDatas) {
                //判断是否有新增数据
                MwModelRancherDataInfoDTO addData = checkExistInMap(newData, oldMap);
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
            changeParam.setHasRancherData(false);
        }
        pIdDataList = pIdDataList.stream().distinct().collect(Collectors.toList());
        Map<String, MwModelRancherDataInfoDTO> parentMap = pIdDataList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s, (
                value1, value2) -> {
            return value2;
        }));
        //父节点数据中，只保留一个最上层根节点。
        Iterator<MwModelRancherDataInfoDTO> iterator = pIdDataList.iterator();
        while (iterator.hasNext()) {
            MwModelRancherDataInfoDTO parentInfo = iterator.next();
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

    private void modelInstanceCopySetting(MwModelRancherDataInfoDTO oldData, MwModelRancherDataInfoDTO newData) {
        newData.setModelIndex(oldData.getModelIndex());
        newData.setModelInstanceId(oldData.getModelInstanceId());
        newData.setModelId(oldData.getModelId());
        newData.setInstanceName(oldData.getInstanceName());
    }

    private MwModelRancherDataInfoDTO checkExistInMap(MwModelRancherDataInfoDTO dataInfo, Map<String, MwModelRancherDataInfoDTO> dataMap) {
        MwModelRancherDataInfoDTO data = dataMap.get(dataInfo.getId());
        return data;
    }

    private List<MwModelRancherDataInfoDTO> getRancherDataInfo(Integer instanceId) throws Exception {
        //根据Rancher获取关联模型属性
        List<Integer> modelIdList = Arrays.asList(RANCHER.getModelId(), CLUSTER.getModelId(), NODES.getModelId(), PROJECTS.getModelId(), NAMESPACE.getModelId());
        List<ModelInfo> modelInfos = mwModelManageDao.selectModelListByIds(modelIdList);
        QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
        param1.setRelationInstanceId(instanceId);
        List<String> modelIndexList = modelInfos.stream().map(ModelInfo::getModelIndex).collect(Collectors.toList());
        param1.setModelIndexs(modelIndexList);
        List<Map<String, Object>> listMap = mwModelViewService.selectInstanceInfoByRelationInstanceId(param1);
        List<MwModelRancherDataInfoDTO> ret = MwModelUtils.convertEsData(MwModelRancherDataInfoDTO.class, listMap);
        return ret;
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
