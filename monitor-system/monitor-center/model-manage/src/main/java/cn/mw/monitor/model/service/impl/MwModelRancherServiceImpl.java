package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.service.graph.EdgeParam;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dao.MwModelVirtualizationDao;
import cn.mw.monitor.model.data.InstanceNotifyType;
import cn.mw.monitor.model.dto.ModelInstanceDto;
import cn.mw.monitor.model.dto.RancherInfo;
import cn.mw.monitor.model.dto.RancherInstanceChangeParam;
import cn.mw.monitor.service.model.dto.rancher.MwModelRancherDataInfoDTO;
import cn.mw.monitor.model.dto.rancher.MwModelRancherProjectUserDTO;
import cn.mw.monitor.model.dto.rancher.MwModelRancherUserDTO;
import cn.mw.monitor.model.dto.rancher.RancherInstance;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.param.rancher.QueryRancherInstanceParam;
import cn.mw.monitor.model.param.rancher.RancherInstanceParam;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelRancherService;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.model.view.ModelRancherTreeView;
import cn.mw.monitor.model.view.RancherView;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mw.monitor.service.model.param.QueryRelationInstanceModelParam;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.*;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.ListMapObjUtils;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.monitor.util.TransferUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.model.param.MatchModelTypeEnum.*;
import static cn.mw.monitor.model.util.MatcherUtils.getNumByStr;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;

/**
 * @author qzg
 * @date 2023/4/17
 */
@Service
@Slf4j
public class MwModelRancherServiceImpl implements MwModelRancherService {
    @Value("${mw.graph.enable}")
    private boolean graphEnable;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Resource
    private MwModelVirtualizationDao mwModelVirtualizationDao;
    @Autowired
    private MwModelRancherRelationManager mwModelRancherRelationManager;
    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;
    @Autowired
    private MwModelViewService mwModelViewService;
    @Autowired
    private MWUserService userService;
    @Autowired(required = false)
    private ConnectionPool connectionPool;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Autowired
    private MWCommonService mwCommonService;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private RancherClientUtils rancherClientUtils;
    @Value("${model.instance.batchFetchNum}")
    private int insBatchFetchNum;

    @Override
    public Reply getAllRancherDataInfo(RancherInstanceParam param) {
        List<MwModelRancherDataInfoDTO> rancherAllList = new ArrayList<>();
        Map<Integer, List<ModelInfo>> modelAndParentInfoListMap = new HashMap<>();
        List<String> modelIndexs = new ArrayList<>();
        List<Integer> modelIdList = Arrays.asList(CLUSTER.getModelId(), NODES.getModelId(), PROJECTS.getModelId(), NAMESPACE.getModelId());
        getPropertyInfos(modelIndexs,modelIdList, modelAndParentInfoListMap);
        //将模型名称当成key值，转为map数据
        log.info("获取rancher关联的index" + modelIndexs);
        try {
            QueryModelInstanceParam qParam = new QueryModelInstanceParam();
            TransferUtils.transferBean(param, qParam);
            //根据Rancher实例获取es数据信息
            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(qParam);
            Integer modelSystemId = 0;
            Integer modelClassifyId = 0;
            String relationName = "";
            String relationIp = "";
            for (Map<String, Object> m : listInfo) {
                log.info("rancher实例信息:"+m);
                if (m.get(MODEL_SYSTEM) != null) {
                    modelSystemId = intValueConvert(m.get(MODEL_SYSTEM));
                    log.info("rancher实例信息:modelSystemId"+modelSystemId);
                }
                if (m.get(MODEL_CLASSIFY) != null) {
                    modelClassifyId = intValueConvert(m.get(MODEL_CLASSIFY));
                    log.info("rancher实例信息:modelClassifyId"+modelClassifyId);
                }

                if (m.get(INSTANCE_NAME_KEY) != null) {
                    relationName = m.get(INSTANCE_NAME_KEY).toString();
                    log.info("rancher实例信息:relationName"+relationName);
                }

                if (m.get(IN_BAND_IP) != null) {
                    relationIp = m.get(IN_BAND_IP).toString();
                    log.info("rancher实例信息:relationIp"+relationIp);
                }

            }
            String modelSystemName = mwModelInstanceDao.getInstanceNameById(modelSystemId);
            String modelClassifyName = mwModelInstanceDao.getInstanceNameById(modelClassifyId);

            log.info("获取rancher设置的modelSystemName" + modelSystemName+";modelClassifyName:"+modelClassifyName);
            List<MwModelMacrosParam> macrosParams = JSON.parseArray(JSONObject.toJSONString(listInfo), MwModelMacrosParam.class);
            log.info("获取rancher登录的macros" + macrosParams);
            //登录rancher，获取数据
            loginRancherAndGetDataInfo(macrosParams, param, rancherAllList);
            //设置业务系统数据（关联rancher容器实例的）
            for(MwModelRancherDataInfoDTO rancherDataInfoDTO : rancherAllList){
                rancherDataInfoDTO.setRelationModelSystem(modelSystemName);
                rancherDataInfoDTO.setRelationModelClassify(modelClassifyName);
                rancherDataInfoDTO.setRelationName(relationName);
                rancherDataInfoDTO.setRelationIp(relationIp);
            }

            log.info("获取rancher数据" + rancherAllList);
            List<RancherInfo> rancherInfos = new ArrayList<>();
            List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();

            //数据校验匹配
            RancherInstanceChangeParam changeParam = mwModelRancherRelationManager.compareRancherInfo(param.getModelInstanceId(), rancherAllList);
            //第一次数据同步
            if (!changeParam.isHasRancherData()) {
                getPropertiesInfoList(rancherAllList, instanceInfoList, rancherInfos, param, modelAndParentInfoListMap);
                log.info("获取rancher插入es的数据:" + instanceInfoList);
                syncAddData(instanceInfoList, rancherInfos, param);
            } else {
                log.info("rancher同步匹配数据:" + changeParam);
                //是否有新增数据
                if (CollectionUtils.isNotEmpty(changeParam.getAddDatas())) {
                    //新增的数据
                    List<MwModelRancherDataInfoDTO> addLists = changeParam.getAddDatas();
                    getPropertiesInfoList(addLists, instanceInfoList, rancherInfos, param, modelAndParentInfoListMap);

                    //父节点数据
                    List<AddAndUpdateModelInstanceParam> instanceInfoListParent = new ArrayList<>();
                    //取父节点数据的rancherInfos，存入neo4j中，形成完整的网状结构，
                    List<MwModelRancherDataInfoDTO> parentLists = changeParam.getPIdDatas();
                    getPropertiesInfoListByEditor(parentLists, instanceInfoListParent, rancherInfos, param, modelAndParentInfoListMap);
                    log.info("获取rancher新增数据:" + addLists + ";获取rancher插入es的数据:" + instanceInfoList);
                    if (CollectionUtils.isNotEmpty(rancherInfos)) {
                        syncAddData(instanceInfoList, rancherInfos, param);
                    }
                }
                //是否有删除的数据
                if (CollectionUtils.isNotEmpty(changeParam.getDeleteDatas())) {
                    List<MwModelRancherDataInfoDTO> deleteLists = changeParam.getDeleteDatas();
                    rancherInfos = new ArrayList<>();
                    instanceInfoList = new ArrayList<>();
                    getPropertiesInfoListByEditor(deleteLists, instanceInfoList, rancherInfos, param, modelAndParentInfoListMap);
                    if (CollectionUtils.isNotEmpty(deleteLists)) {
                        syncDeleteData(deleteLists);
                        if (graphEnable) {
                            //删除neo4j的关系数据
                            mwModelRancherRelationManager.updateRancherInfo(param, rancherInfos, InstanceNotifyType.VirtualSyncDelete);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(changeParam.getUpdateDatas())) {
                    //修改的数据
                    rancherInfos = new ArrayList<>();
                    instanceInfoList = new ArrayList<>();
                    List<MwModelRancherDataInfoDTO> updateLists = changeParam.getUpdateDatas();
                    getPropertiesInfoListByEditor(updateLists, instanceInfoList, rancherInfos, param, modelAndParentInfoListMap);
                    mwModelInstanceDao.batchEditorInstanceName(instanceInfoList);
                    mwModelInstanceService.editorData(instanceInfoList);
                }
            }
        } catch (Exception e) {
            log.error("获取rancher数据失败{}", e);
            return Reply.fail(500, "获取rancher数据失败");
        }
        return Reply.ok(rancherAllList);
    }


    /**
     * 获取Rancher树结构
     *
     * @param param
     * @return
     */
    @Override
    public Reply getRancherDeviceTree(QueryRancherInstanceParam param) {
        try {
            //获取模型信息
            ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoByIndex(param.getModelIndex());
            //遍历关系库
            Session session = connectionPool.getSession();
            NodeParam nodeParam = new NodeParam(modelInfo.getModelId(), param.getModelInstanceId());
            InstanceNode instanceNode = new InstanceNode(nodeParam);
            int maxLevel = ModelAssetUtils.findTreeLevel(session, instanceNode, ModelAssetUtils.COMMON_SPACE + "_" + param.getModelId() + "_" + param.getModelInstanceId());
            RancherView parent = null;
            if (maxLevel > 0) {
                parent = new RancherView();
                GlobalUserInfo globalUser = userService.getGlobalUser();
                List<EdgeParam> edges = ModelAssetUtils.findTreeEdgeBySpace(session, nodeParam, ModelAssetUtils.COMMON_SPACE + "_" + param.getModelId() + "_" + param.getModelInstanceId());
                EdgeParam rootEdge = edges.get(0);
                List<EdgeParam> filterEdges = new ArrayList<>();
                Map<String, String> relationParentMap = edges.stream().collect(Collectors.toMap(s -> String.valueOf(s.getTargetInstanceId()), s -> String.valueOf(s.getSourceInstanceId()), (
                        value1, value2) -> {
                    return value2;
                }));
                if (!globalUser.isSystemUser()) {
                    List<String> allTypeIdList = userService.getAllTypeIdList(globalUser, DataType.INSTANCE_MANAGE);
                    Set<String> idSet = new HashSet<>(allTypeIdList);
                    for (EdgeParam edgeParam : edges) {
                        //和该数据有关联的全部数据
                        if (idSet.contains(String.valueOf(edgeParam.getTargetInstanceId()))) {
                            filterEdges.add(edgeParam);
                        }
                        //获取该数据的全部上级数据
                    }
                    Set<String> sourceIds = new HashSet<>();
                    for (String str : idSet) {
                        String sourceId = relationParentMap.get(str);
                        sourceIds.add(sourceId);
                        getAllParentSourceId(relationParentMap, sourceId, sourceIds);
                    }
                    for (EdgeParam edgeParam : edges) {
                        if (sourceIds.contains(String.valueOf(edgeParam.getTargetInstanceId()))) {
                            filterEdges.add(edgeParam);
                        }
                    }
                    filterEdges = filterEdges.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.getTargetInstanceId()
                            + ";" + s.getSourceInstanceId()))), ArrayList::new));
                } else {
                    filterEdges = edges;
                }

                Map<String, List<String>> edgeMap = new HashMap<>();
                List<Integer> modelIds = new ArrayList<>();
                List<Integer> instanceIds = new ArrayList<>();

                for (EdgeParam edgeParam : filterEdges) {
                    List<String> edgeParamList = edgeMap.get(edgeParam.getSource());
                    if (null == edgeParamList) {
                        edgeParamList = new ArrayList<>();
                        edgeMap.put(edgeParam.getSource(), edgeParamList);
                    }
                    edgeParamList.add(edgeParam.getTarget());

                    if (!modelIds.contains(edgeParam.getSourceModelId())) {
                        modelIds.add(edgeParam.getSourceModelId());
                    }

                    if (!instanceIds.contains(edgeParam.getSourceInstanceId())) {
                        instanceIds.add(edgeParam.getSourceInstanceId());
                    }

                    if (!modelIds.contains(edgeParam.getTargetModelId())) {
                        modelIds.add(edgeParam.getTargetModelId());
                    }

                    if (!instanceIds.contains(edgeParam.getTargetInstanceId())) {
                        instanceIds.add(edgeParam.getTargetInstanceId());
                    }
                }
                //查询模型和实例信息
                Map<String, ModelInfo> modelInfoMap = new HashMap<>();
                List<String> modelIndexList = new ArrayList<>();
                if (modelIds.size() > 0) {
                    List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListByIds(modelIds);
                    for (ModelInfo data : modelInfoList) {
                        modelIndexList.add(data.getModelIndex());
                        modelInfoMap.put(data.getModelId().toString(), data);
                    }
                }
                Map<String, ModelInstanceDto> instanceMap = new HashMap<>();
                Map<String, RancherInstance> esInstanceMap = new HashMap<>();
                List<List<Integer>> instanceIdGroups = new ArrayList<>();
                if (instanceIds.size() > 0) {
                    instanceIdGroups = Lists.partition(instanceIds, insBatchFetchNum);
                    List<ModelInstanceDto> modelInstanceDtos = new ArrayList<>();
                    if (null != instanceIdGroups) {
                        for (List<Integer> instanceIdList : instanceIdGroups) {
                            Map critiria = new HashMap();
                            critiria.put("modelInstanceIds", instanceIdList);
                            if(CollectionUtils.isNotEmpty(instanceIdList)){
                                modelInstanceDtos.addAll(mwModelManageDao.selectModelInstance(critiria));
                            }
                        }
                    }
                    for (ModelInstanceDto data : modelInstanceDtos) {
                        instanceMap.put(data.getInstanceId().toString(), data);
                    }
                    QueryInstanceModelParam queryInstanceModelParam = new QueryInstanceModelParam();
                    queryInstanceModelParam.setInstanceIds(instanceIds);
                    queryInstanceModelParam.setModelIndexs(modelIndexList);
                    List<RancherInstance> rancherInstances = mwModelViewCommonService.getModelListInfoByCommonQuery(RancherInstance.class, null, queryInstanceModelParam);

                    for (RancherInstance rancherInstance : rancherInstances) {
                        esInstanceMap.put(rancherInstance.getModelInstanceId().toString(), rancherInstance);
                    }
                }
                Set<String> visitedSet = new HashSet<>();
                String[] rootValues = rootEdge.getSource().split(EdgeParam.SEP);
                parent.setModelId(rootValues[0]);
                parent.setModelInstanceId(rootValues[1]);
                modelInfoMap.put(modelInfo.getModelId().toString(), modelInfo);//加上顶端的Vcenter数据
                ModelInfo pmodelInfo = modelInfoMap.get(parent.getModelId());
                ModelInstanceDto instanceInfo = instanceMap.get(parent.getModelInstanceId());
                RancherInstance rancherInstance = esInstanceMap.get(parent.getModelInstanceId());
                if (null != modelInfo && null != instanceInfo && null != rancherInstance) {
                    parent.extractInfo(pmodelInfo, instanceInfo, rancherInstance);
                    visitedSet.add(rootEdge.getSource());
                    doVisitEdgeMap(parent, edgeMap, visitedSet, modelInfoMap, instanceMap, esInstanceMap);
                }
            }
            ModelRancherTreeView view = new ModelRancherTreeView();
            if (null != parent) {
                view.addRancherTree(parent);
            }
            return Reply.ok(view);
        } catch (Throwable e) {
            log.error("fail to getRancheDeviceTree cause:{}", e);
            return Reply.fail(500, "获取rancher树结构数据失败");
        }
    }

    @Override
    public Reply getRancherList(QueryRancherInstanceParam param) {
        List<MwModelRancherDataInfoDTO> rancherDataList = new ArrayList<>();
        List<MwModelRancherDataInfoDTO> rancherDataLists = new ArrayList<>();
        PageInfo pageInfo = new PageInfo<List>();
        PageList pageList = new PageList();
        try {
            //根据Rancher获取关联模型属性
            List<Integer> modelIdList = Arrays.asList(CLUSTER.getModelId(), NODES.getModelId(), PROJECTS.getModelId(), NAMESPACE.getModelId());
            List<ModelInfo> modelInfos = mwModelManageDao.selectModelListByIds(modelIdList);
            QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
            param1.setRelationInstanceId(param.getRelationInstanceId());
            List<String> modelIndexList = modelInfos.stream().map(ModelInfo::getModelIndex).collect(Collectors.toList());
            param1.setModelIndexs(modelIndexList);

            List<Map<String, Object>> listMap = mwModelViewService.selectInstanceInfoByRelationInstanceId(param1);
            List<MwModelRancherDataInfoDTO> rancherDataInfoList = JSON.parseArray(JSONObject.toJSONString(listMap), MwModelRancherDataInfoDTO.class);

            Map<String, MwModelRancherDataInfoDTO> modelInstanceIdMap = rancherDataInfoList.stream().collect(Collectors.toMap(MwModelRancherDataInfoDTO::getId, s -> s, (
                    value1, value2) -> {
                return value2;
            }));

            QueryModelInstanceParam qParam = new QueryModelInstanceParam();
            qParam.setModelIndex(param.getRelationModelIndex());
            qParam.setModelInstanceId(param.getRelationInstanceId());
            //根据Rancher实例获取es数据信息
            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(qParam);
            List<MwModelMacrosParam> macrosParams = JSON.parseArray(JSONObject.toJSONString(listInfo), MwModelMacrosParam.class);
            //获取Rancher实例的连接信息，URL、用户名、密码
            String ip = "";
            String tokens = "";
            Integer rancherModelId = 0;
            for (MwModelMacrosParam m : macrosParams) {
                ip = m.getHOST();
                rancherModelId = m.getModelId();
                tokens = RSAUtils.decryptData(m.getTOKENS() != null ? m.getTOKENS() : "", RSAUtils.RSA_PRIVATE_KEY);
            }
            param.setModelId(rancherModelId);
            if (!Strings.isNullOrEmpty(tokens) && !Strings.isNullOrEmpty(ip)) {
                RancherInstanceParam rancherParam = new RancherInstanceParam();
                rancherParam.setIp(ip);
                rancherParam.setTokens(tokens);
                rancherParam.setType(param.getRancherType());
                rancherParam.setModelInstanceId(param.getRelationInstanceId());
                rancherParam.setId(param.getRancherId());
                MatchModelTypeEnum rancherType = MatchModelTypeEnum.getTypeOf(param.getRancherType());
                //点击rancher树的类型级别
                switch (rancherType) {
                    case RANCHER:
                        //列表中table类型
                        if (CLUSTER.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getClusters(rancherParam);
                        }
                        if (PROJECTS.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getProjects(rancherParam);
                        }
                        if (NODES.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getNodes(rancherParam);
                        }
                        if (NAMESPACE.getType().equals(param.getListType())) {
                            for (MwModelRancherDataInfoDTO clusterDTO : rancherClientUtils.getClusters(rancherParam)) {
                                List<MwModelRancherDataInfoDTO> nameSpaceListByclusterId = rancherClientUtils.getNameSpacesByCluster(clusterDTO.getId(), rancherParam);
                                rancherDataList.addAll(nameSpaceListByclusterId);
                            }
                        }
                        break;
                    case CLUSTER:
                        if (CLUSTER.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getClusterById(rancherParam);
                        }
                        if (PROJECTS.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getProjects(rancherParam);
                        }
                        if (NODES.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getNodes(rancherParam);
                        }
                        if (NAMESPACE.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getNameSpacesByCluster(param.getRancherId(), rancherParam);
                        }
                        break;
                    case PROJECTS:
                        if (PROJECTS.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getProjectById(rancherParam);
                        }
                        if (NAMESPACE.getType().equals(param.getListType())) {
                            //获取clusterId下所有的nameSpace(project的PId为clusterId)
                            List<MwModelRancherDataInfoDTO> rancherDataList1 = rancherClientUtils.getNameSpacesByCluster(param.getRancherPId(), rancherParam);
                            //过滤，获取pId为projectId的数据
                            rancherDataList = rancherDataList1.stream().filter(s -> s.getPId().equals(param.getRancherId())).collect(Collectors.toList());
                        }
                        break;
                    case NODES:
                        if (NODES.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getNodeInfoById(rancherParam);
                        }
                        break;
                    case NAMESPACE:
                        if (NAMESPACE.getType().equals(param.getListType())) {
                            rancherDataList = rancherClientUtils.getNameSpacesById(param.getClusterId(), rancherParam);
                        }
                        break;
                    default:
                }
            }
            //设置相关数据
            for (MwModelRancherDataInfoDTO dto : rancherDataList) {
                if (modelInstanceIdMap != null && modelInstanceIdMap.size() > 0 && modelInstanceIdMap.containsKey(dto.getId())) {
                    MwModelRancherDataInfoDTO dataInfoDTO = modelInstanceIdMap.get(dto.getId());
                    dataInfoDTO.setId(dto.getId());
                    dataInfoDTO.setName(dto.getName());
                    dataInfoDTO.setInstanceName(dto.getInstanceName());
                    dataInfoDTO.setType(dto.getType());
                    dataInfoDTO.setCreated(dto.getCreated());
                    dataInfoDTO.setUuid(dto.getUuid());
                    dataInfoDTO.setPId(dto.getPId());
                    dataInfoDTO.setState(dto.getState());
                    dataInfoDTO.setClusterId(dto.getClusterId());
                    rancherDataLists.add(dataInfoDTO);
                }
            }
            rancherDataLists = rancherDataLists.stream().filter(s->!Strings.isNullOrEmpty(s.getInstanceName())).sorted(Comparator.comparing(s->s.getInstanceName())).collect(Collectors.toList());
            //对list进行数据处理，
            rancherDataListHanding(rancherDataLists);
            pageInfo.setTotal(rancherDataLists.size());
            List<MwModelRancherDataInfoDTO>  rancherDataNewLists = pageList.getList(rancherDataLists, param.getPageNumber(), param.getPageSize());
            pageInfo.setList(rancherDataNewLists);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to get treeType:" + param.getRancherType() + ";listType" + param.getListType(), e);
            return Reply.fail(500, "获取" + param.getListType() + "数据失败");
        }
    }

    public void getPropertyInfos(List<String> modelIndexs,List<Integer> modelIdList, Map<Integer, List<ModelInfo>> modelAndParentInfoListMap) {
        //根据Rancher关联模型属性
        List<ModelInfo> modelInfos = mwModelManageDao.selectModelListByIds(modelIdList);
        modelIndexs.addAll(modelInfos.stream().map(ModelInfo::getModelIndex).collect(Collectors.toList()));
        //获取父模型Pids
        List<String> pidsListDis = new ArrayList<>();
        List<String> pidsList = mwModelVirtualizationDao.getPidsByModelIds(modelIdList);
        for (String pids : pidsList) {
            if (!Strings.isNullOrEmpty(pids)) {
                String pidStr = pids.substring(0, pids.length() - 1);
                pidsListDis.addAll(Arrays.asList(pidStr.split(",")));
            }
        }
        pidsListDis = pidsListDis.stream().distinct().collect(Collectors.toList());
        ;
        //获取关联模型的父模型属性
        List<ModelInfo> pIdModelInfo = mwModelManageDao.selectModelInfoByPids(String.join(",", pidsListDis));
        pIdModelInfo.addAll(modelInfos);
        //将模型id作为key值，转为map
        Map<Integer, List<ModelInfo>> modelInfoListMap = pIdModelInfo.stream().collect(Collectors.groupingBy(ModelInfo::getModelId));
        //将模型id作为key值，本体模型和所有父模型做为value，转为map
        for (ModelInfo modelInfo : pIdModelInfo) {
            List<ModelInfo> modelInfoList = new ArrayList<>();
            if (modelInfo.getModelTypeId().intValue() == 1) {
                if (!Strings.isNullOrEmpty(modelInfo.getPids())) {
                    String pids = modelInfo.getPids().substring(0, modelInfo.getPids().length() - 1);
                    List<String> pidList = Arrays.asList(pids.split(","));
                    for (String pid : pidList) {
                        if (!Strings.isNullOrEmpty(pid) && modelInfoListMap != null && modelInfoListMap.get(Integer.valueOf(pid)) != null) {
                            //获取每个模型的父模型集合
                            modelInfoList.addAll(modelInfoListMap.get(Integer.valueOf(pid)));
                        }
                    }
                }
                //将本体模型也加入
                modelInfoList.add(modelInfo);
                // //将模型id作为key值，本体模型和所有父模型list做为value
                modelAndParentInfoListMap.put(modelInfo.getModelId(), modelInfoList);
            }
        }
    }


    /**
     * rancherProject同步设置MW负责人
     *
     * @param param
     * @param projectsList
     * @throws Exception
     */
    private void getRancherProjectUserInfo(RancherInstanceParam param, List<MwModelRancherDataInfoDTO> projectsList) throws Exception {


        //根据Rancher获取关联模型属性
        List<Integer> modelIdList = Arrays.asList(PROJECTS.getModelId());
        List<ModelInfo> modelInfos = mwModelManageDao.selectModelListByIds(modelIdList);
        QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
        param1.setRelationInstanceId(param.getModelInstanceId());
        List<String> modelIndexList = modelInfos.stream().map(ModelInfo::getModelIndex).collect(Collectors.toList());
        param1.setModelIndexs(modelIndexList);
        //查询ES中已存在的project数据
        List<Map<String, Object>> listMap = mwModelViewService.selectInstanceInfoByRelationInstanceId(param1);
        List<MwModelRancherDataInfoDTO> rancherDataInfoList = JSON.parseArray(JSONObject.toJSONString(listMap), MwModelRancherDataInfoDTO.class);

        Map<String, MwModelRancherDataInfoDTO> modelInstanceIdMap = rancherDataInfoList.stream().collect(Collectors.toMap(MwModelRancherDataInfoDTO::getId, s -> s, (
                value1, value2) -> {
            return value2;
        }));


        //获取rancher所有的user信息
        List<MwModelRancherUserDTO> rancherUsersList = rancherClientUtils.getRancherUsers(param);
        //先过滤用户名为空的用户，再将userId作为key，username为value转为Map
        Map<String, String> userRancherNameByIdMap = rancherUsersList.stream().filter(s -> s.getUsername() != null).collect(Collectors.toMap(MwModelRancherUserDTO::getId, MwModelRancherUserDTO::getUsername, (
                value1, value2) -> {
            return value2;
        }));
        log.info("rancher:userRancherNameByIdMap" + rancherUsersList);
        List<MWUser> users = mwModelInstanceDao.selectAllUserList();
        for (MWUser user : users) {
            String loginName = user.getLoginName();
            if (loginName != null && loginName.indexOf("@") != -1) {
                loginName = loginName.split("@")[0];
                user.setLoginName(loginName);
            }
        }
        Map<String, Integer> userMwNameByIdMap = users.stream().collect(Collectors.toMap(MWUser::getLoginName, MWUser::getUserId, (
                value1, value2) -> {
            return value2;
        }));
        log.info("rancher:users" + users);
        //获取项目和用户关系
        List<MwModelRancherProjectUserDTO> rancherProjectUserList = rancherClientUtils.getRancherProjectUser(param);
        log.info("rancher:rancherProjectUserList" + rancherProjectUserList);
        if (userRancherNameByIdMap != null && userRancherNameByIdMap.size() > 0) {
            for (MwModelRancherProjectUserDTO dto : rancherProjectUserList) {
                String userName = userRancherNameByIdMap.get(dto.getUserId());
                if (userName != null && userName.indexOf("@") != -1) {
                    userName = userName.split("@")[0];
                }
                dto.setUserName(userName);
                dto.setMwUserId(userMwNameByIdMap.get(userName));
            }
        }
        Map<String, List<Integer>> userListByProject = rancherProjectUserList.stream().filter(s -> s.getMwUserId() != null).collect(Collectors.groupingBy(MwModelRancherProjectUserDTO::getProjectId, Collectors.mapping(MwModelRancherProjectUserDTO::getMwUserId, Collectors.toList())));
        if (userListByProject != null && userListByProject.size() > 0) {
            for (MwModelRancherDataInfoDTO projectDto : projectsList) {
                if (modelInstanceIdMap.containsKey(projectDto.getId())) {
                    //获取es中存在的project数据，查询用户信息
                    MwModelRancherDataInfoDTO oldEsInfo = modelInstanceIdMap.get(projectDto.getId());
                    //如果project本地设置了用户名，则不同步用户数据
                    if (CollectionUtils.isEmpty(oldEsInfo.getUserIds())) {
                        if (userListByProject.get(projectDto.getId()) != null) {
                            List<Integer> userIdListb = userListByProject.get(projectDto.getId()).stream().distinct().collect(Collectors.toList());
                            projectDto.setUserIds(userIdListb);
                        }
                    }
                }
            }
        }
    }


    /**
     * 对list进行数据处理，
     *
     * @param rancherDataList
     */
    public void rancherDataListHanding(List<MwModelRancherDataInfoDTO> rancherDataList) {
        for (MwModelRancherDataInfoDTO rancherDataInfo : rancherDataList) {
            if (!Strings.isNullOrEmpty(rancherDataInfo.getCpu()) && !Strings.isNullOrEmpty(rancherDataInfo.getRequestedCpu())) {
                double requestCpu = getNumByStr(rancherDataInfo.getRequestedCpu()); //1800m
                double cpu = getNumByStr(rancherDataInfo.getCpu());  //8
                rancherDataInfo.setRequestedCpu((double) requestCpu / 1000 + "");
                rancherDataInfo.setCpuUnit("Cores");
                if (cpu != 0l) {
                    double cpuUtilization = (requestCpu / 1000 / cpu) * 100;
                    BigDecimal bValue = new BigDecimal(cpuUtilization);
                    //保留两位小数
                    String value = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    rancherDataInfo.setCpuUtilization(value + "%");
                } else {
                    rancherDataInfo.setCpuUtilization("0%");
                }

            }
            if (!Strings.isNullOrEmpty(rancherDataInfo.getMemory()) && !Strings.isNullOrEmpty(rancherDataInfo.getRequestedMemory())) {
                String memoryStr = getNumByStr(rancherDataInfo.getMemory()).toString();
                if (!"0".equals(memoryStr)) {
                    String unit = rancherDataInfo.getMemory().replaceAll(memoryStr, "");
                    String requestedMemoryStr = getNumByStr(rancherDataInfo.getRequestedMemory()).toString();
                    String requestedUnit = rancherDataInfo.getRequestedMemory().replaceAll(requestedMemoryStr, "");
                    //单位转换
                    Map map = UnitsUtil.getValueMap(memoryStr, "Gi", unit);
                    Map requestedMap = UnitsUtil.getValueMap(requestedMemoryStr, "Gi", requestedUnit);
                    double cpuUtilization = Double.parseDouble(requestedMap.get("value").toString()) / Double.parseDouble(map.get("value").toString()) * 100;
                    BigDecimal bValue = new BigDecimal(cpuUtilization);
                    //保留两位小数
                    String value = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    //单位转换后的数据
                    rancherDataInfo.setMemory(map.get("value").toString());
                    rancherDataInfo.setRequestedMemory(requestedMap.get("value").toString());
                    rancherDataInfo.setMemoryUnit("GiB");
                    rancherDataInfo.setMemoryUtilization(value + "%");
                } else {
                    rancherDataInfo.setMemoryUtilization("0%");
                }

            }
            if (!Strings.isNullOrEmpty(rancherDataInfo.getPods()) && !Strings.isNullOrEmpty(rancherDataInfo.getRequestedPods())) {
                if (!"0".equals(rancherDataInfo.getPods())) {
                    double podsUtilization = Double.parseDouble(rancherDataInfo.getRequestedPods()) / Double.parseDouble(rancherDataInfo.getPods()) * 100;
                    BigDecimal bValue = new BigDecimal(podsUtilization);
                    //保留两位小数
                    String value = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    rancherDataInfo.setPodsUtilization(value + "%");
                } else {
                    rancherDataInfo.setPodsUtilization("0%");
                }

            }
        }
    }

    private Set<String> getAllParentSourceId(Map<String, String> map, String targetId, Set<String> allSourceIds) {
        if (Strings.isNullOrEmpty(map.get(targetId))) {
            return allSourceIds;
        }
        String sourceId = map.get(targetId);
        allSourceIds.add(sourceId);
        getAllParentSourceId(map, sourceId, allSourceIds);
        return new HashSet<>();
    }

    private void doVisitEdgeMap(RancherView parent, Map<String, List<String>> map, Set<String> visitedSet
            , Map<String, ModelInfo> modelInfoMap, Map<String, ModelInstanceDto> instanceMap, Map<String, RancherInstance> esInstanceMap) {

        String key = parent.getModelId() + EdgeParam.SEP + parent.getModelInstanceId();
        List<String> list = map.get(key);
        if (null != list && list.size() > 0) {
            List<RancherView> childList = new ArrayList<>();
            for (String childValue : list) {
                if (!visitedSet.contains(childValue)) {
                    RancherView child = new RancherView();
                    String[] values = childValue.split(EdgeParam.SEP);
                    child.setModelId(values[0]);
                    child.setModelInstanceId(values[1]);
                    ModelInfo modelInfo = modelInfoMap.get(child.getModelId());
                    ModelInstanceDto instanceInfo = instanceMap.get(child.getModelInstanceId());
                    RancherInstance rancherInstance = esInstanceMap.get(child.getModelInstanceId());
                    if (null == modelInfo) {
                        log.warn("doVisitEdgeMap modelinfo {} is null", child.getModelId());
                        continue;
                    }
                    if (null == instanceInfo) {
                        log.warn("doVisitEdgeMap instanceInfo {} is null", child.getModelInstanceId());
                        continue;
                    }
                    if (null == rancherInstance) {
                        log.warn("esInstanceMap instanceInfo {} is null", child.getModelInstanceId());
                        continue;
                    }

                    child.extractInfo(modelInfo, instanceInfo, rancherInstance);

                    parent.addChildren(child);
                    childList.add(child);
                    visitedSet.add(childValue);
                }
            }

            for (RancherView child : childList) {
                doVisitEdgeMap(child, map, visitedSet, modelInfoMap, instanceMap, esInstanceMap);
            }
        }
    }

    private void getPropertiesInfoList(List<MwModelRancherDataInfoDTO> rancherAllList, List<AddAndUpdateModelInstanceParam> instanceInfoList,
                                       List<RancherInfo> rancherInfos, RancherInstanceParam param, Map<Integer, List<ModelInfo>> modelAndParentInfoListMap) {
        //循环获取到的Rancher设备
        for (MwModelRancherDataInfoDTO info : rancherAllList) {
            if (info != null) {
                if (Strings.isNullOrEmpty(info.getName())) {
                    log.info("没有设备名称的name:" + info);
                    continue;
                }
                Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(info));
                String modelIndex = "";
                Integer modelId = 0;
                String modelName = "";
                List<ModelInfo> modelInfos = modelAndParentInfoListMap.get(MatchModelTypeEnum.getModelId(info.getType()));
                List<PropertyInfo> propertyInfos = new ArrayList<>();
                for (ModelInfo modelInfo : modelInfos) {
                    if (modelInfo.getModelId().equals(MatchModelTypeEnum.getModelId(info.getType()))) {
                        //获取模型本体信息
                        modelIndex = modelInfo.getModelIndex();
                        modelId = modelInfo.getModelId();
                        modelName = modelInfo.getModelName();
                    }
                    propertyInfos.addAll(modelInfo.getPropertyInfos());
                }

                //通过匹配模型名称，获取对应的模型数据
                log.info("获取Rancher的PropertyInfos" + propertyInfos);
                List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
                //虚拟化设备的type和模型的name相同，则该设备加入到模型中

                if (propertyInfos != null && propertyInfos.size() > 0) {
                    for (PropertyInfo propertyInfo : propertyInfos) {
                        AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                        instanceParam.extractFromPropertyInfo(propertyInfo);

//                    TransferUtils.transferBean(p, instanceParam);
                        //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                        instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
                        if (INSTANCE_NAME_KEY.equals(instanceParam.getPropertiesIndexId())) {
                            instanceParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
                            instanceParam.setPropertiesValue(info.getName());
                            instanceParam.setPropertiesType(1);
                        }
                        propertiesParamLists.add(instanceParam);
                    }
                }
                AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                instanceParam.setModelIndex(modelIndex);
                instanceParam.setModelId(modelId);
                instanceParam.setModelName(modelName);
                instanceParam.setInstanceType(DataType.INSTANCE_MANAGE.getName());
                instanceParam.setInstanceName(info.getName());
                instanceParam.setRelationInstanceId(param.getModelInstanceId());
                instanceParam.setInstanceId(info.getModelInstanceId());
                instanceParam.setPropertiesList(propertiesParamLists);
                RancherInfo rancherInfo = new RancherInfo(instanceParam, info);
                rancherInfos.add(rancherInfo);
                if ((!Strings.isNullOrEmpty(instanceParam.getModelIndex()))) {
                    instanceInfoList.add(instanceParam);
                }
            }

        }
    }

    private void getPropertiesInfoListByEditor(List<MwModelRancherDataInfoDTO> rancherAllList, List<AddAndUpdateModelInstanceParam> instanceInfoList,
                                               List<RancherInfo> rancherInfos, RancherInstanceParam param, Map<Integer, List<ModelInfo>> modelAndParentInfoListMap) {
        //获取到的rancherInfos数据
        for (MwModelRancherDataInfoDTO info : rancherAllList) {
            if (info != null) {
                if (Strings.isNullOrEmpty(info.getName())) {
                    log.info("没有设备名称的name:" + info);
                    continue;
                }
                Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(info));
                String modelIndex = "";
                Integer modelId = 0;
                String modelName = "";
                List<ModelInfo> modelInfos = modelAndParentInfoListMap.get(MatchModelTypeEnum.getModelId(info.getType()));
                List<PropertyInfo> propertyInfos = new ArrayList<>();
                for (ModelInfo modelInfo : modelInfos) {
                    if (modelInfo.getModelId().equals(MatchModelTypeEnum.getModelId(info.getType()))) {
                        //获取模型本体信息
                        modelIndex = modelInfo.getModelIndex();
                        modelId = modelInfo.getModelId();
                        modelName = modelInfo.getModelName();
                    }
                    propertyInfos.addAll(modelInfo.getPropertyInfos());
                }

                //通过匹配模型名称，获取对应的模型数据
                log.info("获取Rancher的PropertyInfos" + propertyInfos);
                List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
                //虚拟化设备的type和模型的name相同，则该设备加入到模型中

                if (propertyInfos != null && propertyInfos.size() > 0) {
                    for (PropertyInfo propertyInfo : propertyInfos) {
                        AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                        instanceParam.extractFromPropertyInfo(propertyInfo);

//                    TransferUtils.transferBean(p, instanceParam);
                        //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                        instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
                        if (INSTANCE_NAME_KEY.equals(instanceParam.getPropertiesIndexId())) {
                            instanceParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
                            instanceParam.setPropertiesValue(info.getName());
                            instanceParam.setPropertiesType(1);
                        }
                        propertiesParamLists.add(instanceParam);
                    }
                }
                AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                instanceParam.setModelIndex(info.getModelIndex());
                instanceParam.setModelId(info.getModelId());
                instanceParam.setInstanceType(DataType.INSTANCE_MANAGE.getName());
                instanceParam.setInstanceName(info.getName());
                instanceParam.setInstanceId(info.getModelInstanceId());
                instanceParam.setEsId(info.getModelIndex() + info.getModelInstanceId());
                instanceParam.setRelationInstanceId(param.getModelInstanceId());
                instanceParam.setPropertiesList(propertiesParamLists);
                RancherInfo rancherInfo = new RancherInfo(instanceParam, info);
                rancherInfos.add(rancherInfo);
                if ((!Strings.isNullOrEmpty(instanceParam.getModelIndex()))) {
                    instanceInfoList.add(instanceParam);
                }
            }
        }
    }

    private void loginRancherAndGetDataInfo(List<MwModelMacrosParam> macrosParams, RancherInstanceParam param,
                                            List<MwModelRancherDataInfoDTO> rancherAllList) throws Exception {
        //获取虚拟化VCenter的连接信息，URL、用户名、密码
        String ip = "";
        String tokens = "";
        Integer rancherModelId = 0;
        for (MwModelMacrosParam m : macrosParams) {
            ip = m.getHOST();
            rancherModelId = m.getModelId();
            tokens = RSAUtils.decryptData(m.getTOKENS() != null ? m.getTOKENS() : "", RSAUtils.RSA_PRIVATE_KEY);
        }
        param.setModelId(rancherModelId);
        param.setTokens(tokens);
        param.setIp(ip);
        log.info("rancher 登录参数:" + param);
        if (!Strings.isNullOrEmpty(tokens) && !Strings.isNullOrEmpty(ip)) {
            List<MwModelRancherDataInfoDTO> clusterList = null;
            clusterList = rancherClientUtils.getClusters(param);
            rancherAllList.addAll(clusterList);
            List<MwModelRancherDataInfoDTO> projectsList = rancherClientUtils.getProjects(param);
            getRancherProjectUserInfo(param, projectsList);
            rancherAllList.addAll(projectsList);
            List<MwModelRancherDataInfoDTO> nodesList = rancherClientUtils.getNodes(param);
            rancherAllList.addAll(nodesList);
            for (MwModelRancherDataInfoDTO clusterDTO : clusterList) {
                List<MwModelRancherDataInfoDTO> nameSpaceListByclusterId = rancherClientUtils.getNameSpacesByCluster(clusterDTO.getId(), param);
                rancherAllList.addAll(nameSpaceListByclusterId);
            }
        }
    }


    @Override
    public Reply setRancherUser(ModelRelationInstanceUserListParam params) {
        try {
            setModelInstancePerUser(params);
            return Reply.ok("新增关联负责人成功！");
        } catch (Exception e) {
            return Reply.fail(500, "新增关联负责人失败");
        }
    }

    /**
     * 查询虚拟化权限
     *
     * @param param
     * @return
     */
    @Override
    public Reply getRancherUser(ModelRelationInstanceUserParam param) {
        try {
            List<ModelRelationInstanceUserParam> list = new ArrayList<>();
            param.setType(DataType.INSTANCE_MANAGE);
            list = getModelInstancePerUser(param);
            return Reply.ok(list);
        } catch (Exception e) {
            return Reply.fail(500, "查询资产权限数据失败");
        }
    }

    @Override
    public List<ModelRelationInstanceUserParam> getModelInstancePerUser(ModelRelationInstanceUserParam param) {
        List<ModelRelationInstanceUserParam> list = new ArrayList<>();
        DataPermission dataPermission = mwCommonService.getDataPermissionDetail(param.getType(), param.getTypeId());
        ModelRelationInstanceUserParam model = new ModelRelationInstanceUserParam();
        model.setTypeId(param.getTypeId());
        model.setGroupIds(dataPermission.getGroupIds());
        model.setGroups(dataPermission.getGroups());
        model.setUserIds(dataPermission.getUserIds());
        model.setPrincipal(dataPermission.getPrincipal());
        model.setOrgIds(dataPermission.getOrgNodes());
        model.setDepartment(dataPermission.getDepartment());
        list.add(model);
        return list;
    }

    @Override
    public void setModelInstancePerUser(ModelRelationInstanceUserListParam params) {
        //绑定机构
        List<OrgMapper> orgMapper = new ArrayList<>();
        List<GroupMapper> groupMapper = new ArrayList<>();
        List<UserMapper> userMapper = new ArrayList<>();
        List<DataPermissionDto> permissionMapper = new ArrayList<>();
        List<String> typeIds = new ArrayList<>();
        String type = DataType.INSTANCE_MANAGE.getName();
        for (ModelRelationInstanceUserParam qParam : params.getParamList()) {
            String typeId = qParam.getTypeId();
            typeIds.add(typeId);
            DataPermissionDto dto = new DataPermissionDto();
            dto.setType(type);     //类型
            dto.setTypeId(typeId);  //数据主键
            dto.setDescription(DataType.valueOf(type).getDesc()); //描述
            List<Integer> userIdList = qParam.getUserIds();
            List<List<Integer>> orgIdList = qParam.getOrgIds();
            List<Integer> groupIdList = qParam.getGroupIds();

            orgIdList.forEach(
                    orgId -> orgMapper.add(OrgMapper.builder().typeId(typeId).orgId(orgId.get(orgId.size() - 1)).type(type).build())
            );
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                dto.setIsGroup(1);
            } else {
                dto.setIsGroup(0);
            }
            groupIdList.forEach(
                    groupId -> groupMapper.add(GroupMapper.builder().typeId(typeId).groupId(groupId).type(type).build())
            );
            if (CollectionUtils.isNotEmpty(userIdList)) {
                dto.setIsUser(1);
            } else {
                dto.setIsUser(0);
            }
            userIdList.forEach(userIds -> {
                        log.info("userMapper.add,userid:{}", userIds);
                        userMapper.add(UserMapper.builder().typeId(typeId).userId(userIds).type(type).build());
                    }
            );
            permissionMapper.add(dto);
        }
        DeleteDto deleteDto = DeleteDto.builder()
                .typeIds(typeIds)
                .type(type)
                .build();
        mwCommonService.deleteMapperAndPerms(deleteDto);
        if(CollectionUtils.isNotEmpty(groupMapper)){
            mwCommonService.insertGroupMapper(groupMapper);
        }
        if(CollectionUtils.isNotEmpty(userMapper)){
            mwCommonService.insertUserMapper(userMapper);
        }
        if(CollectionUtils.isNotEmpty(orgMapper)){
            mwCommonService.insertOrgMapper(orgMapper);
        }
        if(CollectionUtils.isNotEmpty(permissionMapper)){
            mwCommonService.insertPermissionMapper(permissionMapper);
        }
    }

    private void syncAddData(List<AddAndUpdateModelInstanceParam> instanceInfoList, List<RancherInfo> rancherInfos, RancherInstanceParam param) throws Exception {
        log.info("获取rancher插入es的数据:" + instanceInfoList);
        if (instanceInfoList != null && instanceInfoList.size() > 0) {
            mwModelInstanceService.saveData(instanceInfoList, true, true);
        }
        if (graphEnable) {
            //保存虚拟化设备拓扑关系
            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
            instanceParam.setInstanceId(param.getModelInstanceId());
            instanceParam.setModelIndex(param.getModelIndex());
            instanceParam.setModelId(param.getModelId());
            MwModelRancherDataInfoDTO rancherDataInfoDTO = new MwModelRancherDataInfoDTO();
            rancherDataInfoDTO.setType(RANCHER.getType());
            rancherDataInfoDTO.setId(param.getModelInstanceId() + "");
            RancherInfo rancherInfo = new RancherInfo(instanceParam, rancherDataInfoDTO);
            rancherInfos.add(rancherInfo);
            //获取节点和线数据，存入neo4j数据
            mwModelRancherRelationManager.updateRancherInfo(param, rancherInfos, InstanceNotifyType.VirtualSyncInit);
            log.info("存入neo4j数据成功");
        }
    }

    private void syncDeleteData(List<MwModelRancherDataInfoDTO> deleteLists) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        List<Integer> delInstanceIds = new ArrayList<>();
        //es删除数据
        for (MwModelRancherDataInfoDTO mwModelRancherDataInfoDTO : deleteLists) {
            String modelIndex = mwModelRancherDataInfoDTO.getModelIndex();
            Integer instanceId = mwModelRancherDataInfoDTO.getModelInstanceId();
            String esId = modelIndex + instanceId;
            bulkRequest.add(new DeleteRequest(modelIndex, esId));
            delInstanceIds.add(instanceId);
            if (bulkRequest.estimatedSizeInBytes() > 5 * 1024 * 1024) {//每次删除5Mb的数据
                restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                bulkRequest = new BulkRequest();
            }
            if (delInstanceIds.size() > 800) {
                //删除mysql数据
                mwModelInstanceDao.deleteBatchInstanceById(delInstanceIds);
                delInstanceIds = new ArrayList<>();
            }
        }
        if (bulkRequest.estimatedSizeInBytes() > 0) {
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        }
        if (delInstanceIds.size() > 0) {
            //删除mysql数据
            mwModelInstanceDao.deleteBatchInstanceById(delInstanceIds);
        }
    }

}
