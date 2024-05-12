package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.graph.modelAsset.*;
import cn.mw.monitor.model.dao.MwInstanceViewDao;
import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dao.MwModelRelationsDao;
import cn.mw.monitor.model.data.Convert2Neo4jData;
import cn.mw.monitor.model.data.ModelConn;
import cn.mw.monitor.model.data.SortNewEdge;
import cn.mw.monitor.model.data.SortNode;
import cn.mw.monitor.model.dto.*;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MwModelRelationsService;
import cn.mw.monitor.model.type.InstanceActionType;
import cn.mw.monitor.model.util.ModelUtils;
import cn.mw.monitor.model.view.*;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.graph.EdgeParam;
import cn.mw.monitor.service.graph.InstanceModelMapper;
import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ListMapObjUtils;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

/**
 * @author qzg
 * @date 2022/2/21
 */
@Service
@Slf4j
public class MwModelRelationsServiceImpl implements MwModelRelationsService {
    @Resource
    MwModelRelationsDao mwModelRelationsDao;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Resource
    MwModelInstanceDao mwModelInstanceDao;

    @Resource
    private MwModelManageDao mwModelManageDao;

    @Resource
    private MwInstanceViewDao mwInstanceViewDao;

    @Autowired(required = false)
    private ConnectionPool connectionPool;

    @Autowired
    private ModuleIDManager moduleIDManager;


    /**
     * 模型关系新增
     * 创建模型关系的时候，不能把关系变成一个环
     * 创建关系的时候要判断一下该关系是否已经存在，如果存在则创建不成功
     * <p>
     * 关系关联是个对立的，在当前模型下关联了对端模型，涉及到关系中的两个模型，
     * 都是相互影响的。在对端模型下，也就意味着关联了该当前模型。
     * 采用双数据存储方式
     *
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Reply creatModelRelations(AddAndUpdateModelRelationParam param) {
        try {
            Session session = connectionPool.getSession();

            //查询已有节点信息
            ModelAsset modelAsset = session.load(ModelAsset.class, param.getOwnModelId());
            if (null == modelAsset) {
                modelAsset = new ModelAsset();
                modelAsset.setId(param.getOwnModelId());
                doCreate(modelAsset, param, session);
            } else {
                //检查关系是否已经存在
                String newRelationKey = ModelUtils.genRelationKey(param.getOwnModelId().toString(), param.getOppositeModelId().toString());
                boolean isExist = false;
                List<ModelRelate> modelRelateList = modelAsset.getModelRelates();
                if (null != modelRelateList) {
                    for (ModelRelate modelRelate : modelRelateList) {
                        String key = ModelUtils.genRelationKey(modelRelate.getStartNode().getId().toString()
                                , modelRelate.getEndNode().getId().toString());
                        if (newRelationKey.equals(key)) {
                            isExist = true;
                            break;
                        }
                    }
                }

                if (!isExist) {
                    doCreate(modelAsset, param, session);
                } else {
                    return Reply.fail("关系已经存在");
                }
            }

            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to creatModelRelations param{}, case by {}", param, e);
            return Reply.fail(500, "模型关系新增失败");
        }
    }

    private void doCreate(ModelAsset start, AddAndUpdateModelRelationParam param, Session session) {

        ModelAsset oppoModelAsset = new ModelAsset();
        oppoModelAsset.setId(param.getOppositeModelId());

        ModelRelate modelRelate = new ModelRelate();
        start.addModelRelate(modelRelate);

        ModelAsset startNode = new ModelAsset();
        startNode.extractFrom(start);
        modelRelate.setStartNode(startNode);
        modelRelate.setEndNode(oppoModelAsset);

        ModelRelationDTO modelRelationDTO = new ModelRelationDTO();
        ModelRelationInfo modelRelationInfo = ModelUtils.genOwnModelRelationInfo(param);
        modelRelationDTO.addModelRelationInfo(modelRelationInfo);
        modelRelationDTO.addGroupId(param.getRelationGroupId());

        //查询对端模型id对应的默认分组是否存在,如果不存在则创建
        //由于对端此时并未设置关系所对应的分组,此时应放到默认分组下
        Integer defaulGroupId = mwModelRelationsDao.findDefaulGroupId(param.getOppositeModelId());
        if (null == defaulGroupId) {
            AddAndUpdateModelRelationGroupParam params = new AddAndUpdateModelRelationGroupParam();
            params.setOwnModelId(param.getOppositeModelId());
            params.setRelationGroupName(UNGROUP_NAME);
            params.setRelationGroupDesc(UNGROUP_DESC);
            params.setDefautGroupFlag(true);
            creatModelRelationsGroup(params);
            modelRelationDTO.addGroupId(params.getId());
        } else {
            modelRelationDTO.addGroupId(defaulGroupId);
        }

        modelRelationInfo = ModelUtils.genOppoModelRelationInfo(param);
        modelRelationDTO.addModelRelationInfo(modelRelationInfo);
        modelRelate.setModelRelationDTO(modelRelationDTO);

        session.save(start);
    }

    /**
     * 获取关联模型数据
     *
     * @param ownModelId
     * @return
     */
    @Override
    public Reply selectAllModelByRelationsExludeOwn(Integer ownModelId) {
        try {
            List<Map> modelInfo = mwModelRelationsDao.selectAllModelByRelationsExludeOwn(ownModelId);
            return Reply.ok(modelInfo);
        } catch (Exception e) {
            log.error("fail to selectAllModelByRelationsExludeOwn param{}, case by {}", "", e);
            return Reply.fail(500, "获取关联模型数据失败");
        }
    }

    /**
     * 模型关系展示
     * 为前端拼凑数据
     *
     * @return
     */
    @Override
    @Transactional
    public Reply showModelRelations(AddAndUpdateModelRelationParam param) {
        try {

            //查询模型信息
            ModelInfo modelInfo = mwModelManageDao.selectBaseModelInfoById(param.getOwnModelId());

            //查询分组信息
            List<RelationGroupDTO> groupInfoList = mwModelRelationsDao.getRelationGroupListInfo(param.getOwnModelId());

            //根据id获取直接关联的模型
            Session session = connectionPool.getSession();
            String query = genDirectRelateModelById(null);
            Map criteria = new HashMap();
            criteria.put("id", param.getOwnModelId());
            Result result = session.query(query, criteria);

            if (null != groupInfoList) {
                for (RelationGroupDTO relationGroupDTO : groupInfoList) {
                    log.info(relationGroupDTO.toString());
                }
            }
            //封装返回信息
            ModelRelationView modelRelationView = new ModelRelationView();
            modelRelationView.extractFrom(result.iterator(), groupInfoList, modelInfo);
            return Reply.ok(modelRelationView);
        } catch (Exception e) {
            log.error("fail to showModelRelations param{}, case by {}", "", e);
            return Reply.fail(500, "查询模型关系展示数据失败");
        }
    }

    private Integer checkAndGetOppoModelId(Iterator iterator, Integer ownModelId) {
        Integer ret = null;
        while (iterator.hasNext()) {
            Map<String, Object> data = (Map) iterator.next();
            Object obj = data.get(ModelUtils.RELATION_KEY);
            if (obj instanceof ModelRelate) {
                ModelRelate modelRelate = (ModelRelate) obj;
                ModelRelationDTO modelRelationDTO = modelRelate.getModelRelationDTO();
                Map map = modelRelationDTO.getRelationInfoMap();
                for (Object mapData : map.values()) {
                    try {
                        ModelRelationInfo modelRelationInfo = ListMapObjUtils.mapToBean((Map) mapData, ModelRelationInfo.class);
                        if (modelRelationInfo.getModelId() != ownModelId) {
                            ret = modelRelationInfo.getModelId();
                        }
                    } catch (Exception e) {
                        log.warn("extractFrom {}", e.toString());
                    }
                }
            }
        }
        return ret;
    }

    private String genDirectRelateModelById(String ret) {
        StringBuffer sb = new StringBuffer("match (").append(ModelUtils.OWN_KEY).append(":ModelAsset)")
                .append("-[").append(ModelUtils.RELATION_KEY).append(":RelateModel]")
                .append("-(").append(ModelUtils.OPPO_KEY).append(":ModelAsset)")
                .append(" where ").append(ModelUtils.OWN_KEY).append(".id = $id");
        if (StringUtils.isNotEmpty(ret)) {
            sb.append(ret);
        } else {
            sb.append(" return ").append(ModelUtils.OWN_KEY)
                    .append(",").append(ModelUtils.RELATION_KEY).append(",").append(ModelUtils.OPPO_KEY)
            ;
        }
        return sb.toString();
    }

    @Override
    public Reply selectModelRelationsByModelId(AddAndUpdateModelRelationParam param) {
        try {
            Session session = connectionPool.getSession();
            ModelAsset startModel = session.load(ModelAsset.class, param.getOwnModelId(), 1);
            if (null != startModel && null != startModel.getModelRelates()) {
                ModelRelate selectModelRelate = null;
                for (ModelRelate modelRelate : startModel.getModelRelates()) {
                    if (modelRelate.getStartNode().getId().equals(param.getOwnModelId())
                            && modelRelate.getEndNode().getId().equals(param.getOppositeModelId())) {
                        selectModelRelate = modelRelate;
                        break;
                    }

                    if (modelRelate.getEndNode().getId().equals(param.getOwnModelId())
                            && modelRelate.getStartNode().getId().equals(param.getOppositeModelId())) {
                        selectModelRelate = modelRelate;
                        break;
                    }
                }

                if (null != selectModelRelate) {
                    {
                        Integer ownModelId = param.getOwnModelId();
                        Integer oppoModelId = param.getOppositeModelId();
                        //查询分组信息
                        List<RelationGroupDTO> groupInfoList = mwModelRelationsDao.getRelationGroupListInfo(ownModelId);

                        //查询本端模型信息
                        ModelInfo ownModelInfo = mwModelManageDao.selectBaseModelInfoById(ownModelId);
                        ModelInfo oppoModelInfo = mwModelManageDao.selectBaseModelInfoById(oppoModelId);

                        //找到本端模型id对应的关系组信息
                        ModelRelationDTO modelRelationDTO = selectModelRelate.getModelRelationDTO();
                        if (null != modelRelationDTO) {
                            RelationGroupDTO ownGroup = null;
                            for (RelationGroupDTO relationGroupDTO : groupInfoList) {
                                if (modelRelationDTO.getGroupIds().contains(relationGroupDTO.getRealGroupId())) {
                                    ownGroup = relationGroupDTO;
                                    break;
                                }
                            }

                            Map map = (Map) modelRelationDTO.getRelationInfoMap().get(ownModelId);
                            ModelRelationInfo ownModelRelationInfo = ListMapObjUtils.mapToBean(map, ModelRelationInfo.class);

                            map = (Map) modelRelationDTO.getRelationInfoMap().get(oppoModelId);
                            ModelRelationInfo oppoModelRelationInfo = ListMapObjUtils.mapToBean(map, ModelRelationInfo.class);

                            AddAndUpdateModelRelationParam info = new AddAndUpdateModelRelationParam();
                            info.setRelationGroupId(ownGroup.getRealGroupId());
                            info.setOwnModelId(ownModelId);
                            info.setOwnModelName(ownModelInfo.getModelName());
                            info.setOwnRelationNum(ownModelRelationInfo.getNum());
                            info.setOwnRelationName(ownModelRelationInfo.getRelationName());
                            info.setOwnRelationId(ownModelRelationInfo.getId());

                            info.setOppositeModelId(oppoModelId);
                            info.setOppositeModelName(oppoModelInfo.getModelName());
                            info.setOppositeRelationNum(oppoModelRelationInfo.getNum());
                            info.setOppositeRelationName(oppoModelRelationInfo.getRelationName());
                            info.setOppositeRelationId(oppoModelRelationInfo.getId());
                            return Reply.ok(info);
                        }

                    }
                }
            }
        } catch (Exception e) {
            log.error("fail to selectModelRelationsByModelId param{}, case by {}", "", e);
            return Reply.fail(500, "查询模型关系数据失败");
        }

        return Reply.ok();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Reply editorModelRelationsByModelId(AddAndUpdateModelRelationParam param) {
        try {
            Session session = connectionPool.getSession();
            ModelAsset startModel = session.load(ModelAsset.class, param.getOwnModelId(), 1);
            if (null != startModel && null != startModel.getModelRelates()) {
                //找到需要修改的关系
                ModelRelate selectModelRelate = null;
                for (ModelRelate modelRelate : startModel.getModelRelates()) {
                    if (modelRelate.getEndNode().getId().equals(param.getOppositeModelId())) {
                        selectModelRelate = modelRelate;
                        break;
                    }
                }

                ModelRelationDTO modelRelationDTO = selectModelRelate.getModelRelationDTO();
                if (null != modelRelationDTO) {
                    modelRelationDTO.getRelationInfoMap().clear();
                    ModelRelationInfo modelRelationInfo = ModelUtils.genOwnModelRelationInfo(param);
                    modelRelationDTO.addModelRelationInfo(modelRelationInfo);

                    modelRelationInfo = ModelUtils.genOppoModelRelationInfo(param);
                    modelRelationDTO.addModelRelationInfo(modelRelationInfo);
                }

                session.save(startModel);
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to editorModelRelationsByModelId param{}, case by {}", "", e);
            return Reply.fail(500, "修改模型关系失败");
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Reply deleteModelRelations(AddAndUpdateModelRelationParam param) {
        try {
            Session session = connectionPool.getSession();
            //查询已有节点信息
            ModelAsset modelAsset = session.load(ModelAsset.class, param.getOwnModelId(), 1);
            if (null != modelAsset) {
                //检查关系是否已经存在
                String newRelationKey = ModelUtils.genRelationKey(param.getOwnModelId().toString(), param.getOppositeModelId().toString());

                ModelRelate pendingDelModelRelate = null;
                Iterator ite = modelAsset.getModelRelates().iterator();
                while (ite.hasNext()) {
                    ModelRelate modelRelate = (ModelRelate) ite.next();
                    String key = ModelUtils.genRelationKey(modelRelate.getStartNode().getId().toString()
                            , modelRelate.getEndNode().getId().toString());
                    if (newRelationKey.equals(key)) {
                        pendingDelModelRelate = modelRelate;
                        break;
                    }
                }

                if (null != pendingDelModelRelate) {
                    session.delete(pendingDelModelRelate);
                }
            }

            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to deleteModelRelations param{}, case by {}", "", e);
            return Reply.fail(500, "删除模型关系失败");
        }
    }

    @Override
    public Reply creatModelRelationsGroup(AddAndUpdateModelRelationGroupParam param) {
        try {
            param.setCreator(iLoginCacheInfo.getLoginName());
            param.setModifier(iLoginCacheInfo.getLoginName());
            mwModelRelationsDao.creatModelRelationsGroup(param);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to creatModelRelationsGroup param{}, case by {}", "", e);
            return Reply.fail(500, "新增关系分组数据失败");
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Reply updateModelRelationsGroup(AddAndUpdateModelRelationGroupParam param) {
        param.setModifier(iLoginCacheInfo.getLoginName());
        mwModelRelationsDao.updateModelRelationsGroup(param);
        return Reply.ok();
    }

    @Override
    public Reply modelRelationsGroupByUpdate(AddAndUpdateModelRelationGroupParam param) {
        AddAndUpdateModelRelationGroupParam data = mwModelRelationsDao.modelRelationsGroupByUpdate(param.getRelationGroupId());
        return Reply.ok(data);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Reply deleteModelRelationsGroup(DeleteModelRelationGroupParam param) {
        try {
            //删除之前，先判断分组下是否有关系数据存在
            Integer groupId = param.getRelationGroupId();
            ModelRelationGroupSelParam selParam = new ModelRelationGroupSelParam();
            selParam.setId(groupId);
            List<ModelRelationGroupDTO> list = mwModelRelationsDao.selectModelRelationGroup(selParam);
            boolean isExistRelation = false;
            if (null != list && list.size() > 0) {
                ModelRelationGroupDTO modelRelationGroupDTO = list.get(0);
                Session session = connectionPool.getSession();
                ModelAsset modelAsset = session.load(ModelAsset.class, modelRelationGroupDTO.getOwnModelId(), 1);

                //检查分组下是否存在关系
                if (null != modelAsset) {
                    List<ModelRelate> modelRelateList = modelAsset.getModelRelates();
                    if (null != modelRelateList) {
                        for (ModelRelate modelRelate : modelRelateList) {
                            ModelRelationDTO modelRelationDTO = modelRelate.getModelRelationDTO();
                            if (modelRelationDTO.getGroupIds().contains(groupId)) {
                                isExistRelation = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!isExistRelation) {
                mwModelRelationsDao.deleteModelRelationsGroup(param);
            } else {
                return Reply.fail(500, "该分组下有数据存在，不可删除。");
            }

            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to selectAllModelByRelations param{}, case by {}", "", e);
            return Reply.fail(500, "删除关系分组数据失败");
        }
    }

    @Override
    public boolean hasRelation(Integer modelId) throws Exception {
        Session session = connectionPool.getSession();
        if (null != session) {
            ModelAsset modelAsset = session.load(ModelAsset.class, modelId, 1);
            List<ModelRelate> modelRelateList = modelAsset.getModelRelates();
            if (null != modelRelateList && modelRelateList.size() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteModelNode(Integer modelId) throws Exception {
        Session session = connectionPool.getSession();
        if (null != session) {
            ModelAsset modelAsset = new ModelAsset();
            modelAsset.setId(modelId);
            session.delete(modelAsset);
        }
    }

    @Override
    public Reply selectGroupList(QueryGroupListParam param) {
        List<Map<String, Object>> list = mwModelRelationsDao.selectGroupList(param);
        return Reply.ok(list);
    }


    @Override
    public Reply queryModelRelationGroupBySelect(ModelRelationGroupsParam param) {
        try {
            List<Map> list = mwModelRelationsDao.queryModelRelationGroupBySelect(param.getOwnModelId());
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to queryModelRelationGroupBySelect param{}, case by {}", "", e);
            return Reply.fail(500, "查询关系分组下拉数据失败");
        }
    }

    /**
     * 实例拓扑保存数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply addInstanceToPo(QueryInstanceRelationToPoParam param) {
        try {
            Map<Integer, ComboParam> modelMap = new HashMap<>();
            if (null != param.getLastData()) {
                LastData lastData = param.getLastData();

                if (null != lastData.getCombos()) {
                    for (ComboParam comboParam : lastData.getCombos()) {
                        modelMap.put(comboParam.getId(), comboParam);
                    }
                }

                //构造实例拓扑结构
                if (null != lastData.getEdges()) {
                    Convert2Neo4jData convert2Neo4jData = new Convert2Neo4jData();
                    convert2Neo4jData.convertFrom(lastData.getNodes(), lastData.getEdges());
                    NodeParam rootNodeParam = convert2Neo4jData.getRootNodeParam();

                    //查询当前实例的拓扑信息,并更新拓扑信息
                    Map criteria = new HashMap();
                    Integer instanceId = rootNodeParam.getRealId();
                    criteria.put("modelInstanceId", instanceId);
                    criteria.put("instanceViewId", param.getInstanceViewId());
                    List<ModelInstanceDto> list = mwModelManageDao.selectModelInstance(criteria);
                    if (null != list && list.size() > 0) {

                        //当存在实例视图id时,才保存拓扑信息
                        if (null != param.getInstanceViewId()) {

                            //获取隐藏,显示的拓扑信息
                            ModelInstanceTopoInfo modelInstanceTopoInfo = list.get(0).getTopoInfo();
                            AddAndUpdateInstanceTopoInfoParam updateParam = new AddAndUpdateInstanceTopoInfoParam();
                            updateParam.setInstanceId(instanceId);

                            updateParam.setInstanceViewId(param.getInstanceViewId());

                            ModelInstanceTopoInfo newTopoInfo = new ModelInstanceTopoInfo();
                            List<Integer> hideModelIds = param.getHideModelIds();
                            List<Integer> showModelIds = param.getShowModelIds();

                            newTopoInfo.setHideModelIds(hideModelIds);
                            newTopoInfo.setShowModelIds(showModelIds);

                            //只保存节点及分组框信息,不保留连接信息
                            lastData.setEdges(null);
                            newTopoInfo.setLastData(lastData);

                            //当没有隐藏信息时,页面显示拓扑和实际完整拓扑是一样的,此时不设置页面显示拓扑
                            if ((null != hideModelIds && hideModelIds.size() > 0)
                                    || (null != showModelIds && showModelIds.size() > 0)) {
                                LastData data = param.getData();
                                data.setEdges(null);
                                newTopoInfo.setData(data);
                            }
                            String json = JSON.toJSONString(newTopoInfo);
                            updateParam.setTopoInfo(json);

                            if (null != modelInstanceTopoInfo) {
                                if ((null != param.getHideModelIds() && param.getHideModelIds().size() > 0)
                                        || (null != param.getShowModelIds() && param.getShowModelIds().size() > 0)) {
                                    mwModelManageDao.updateModelInstanceTopoInfo(updateParam);
                                }
                            } else {
                                String id = UUIDUtils.getUUID();
                                updateParam.setId(id);
                                mwModelManageDao.insertModelInstanceTopoInfo(updateParam);
                            }
                        }

                        //保存拓扑到neo4j,实例拓扑删除和添加时,保存当前拓扑状态lastData
                        //根据根节点生成拓扑的空间
                        Session session = connectionPool.getSession();

                        //重新保存实例id标签空间节点
                        //判断是否保存到用户自定义的视图
                        String space = ModelAssetUtils.INSTANCE_PRE + rootNodeParam.getRealId();
                        if (null != param.getInstanceViewId() && param.getInstanceViewId() > 0) {
                            space = ModelAssetUtils.INSTANCE_VIEW_SPACE + param.getInstanceViewId();
                        }

                        ModelAssetUtils.deleteInstanceTopo(session, space);
                        ModelAssetUtils.addInstanceTopo(session, space, convert2Neo4jData.getNodes(), convert2Neo4jData.getLines());

                    }
                }
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to addInstanceToPo param{}, case by {}", "", e);
            return Reply.fail(500, "实例拓扑保存数据失败");
        }
    }

    private void getToPOIdByHistroy(ModelRelationToPoParam modelRelationToPoParam, List<AddAndUpdateRelationToPoParam> topoAllInfo, List<AddAndUpdateRelationToPoParam> addList, Boolean isAdd) {
        List<AddAndUpdateRelationToPoParam> list = mwModelRelationsDao.selectAllRelationsInfoIdByToPo(modelRelationToPoParam);
        addList.addAll(list);
        for (AddAndUpdateRelationToPoParam aParam : list) {
            getToPOInfoByChild(aParam, topoAllInfo, addList, isAdd);
        }

    }

    private void getToPOInfoByChild(AddAndUpdateRelationToPoParam aParam, List<AddAndUpdateRelationToPoParam> topoAllInfo, List<AddAndUpdateRelationToPoParam> addList, Boolean isAdd) {
        try {
            for (AddAndUpdateRelationToPoParam param : topoAllInfo) {
                int paramOwnModelId = param.getOwnModelId().intValue();
                int paramOwnInstanceId = param.getOwnInstanceId().intValue();
                int paramOppositeModelId = param.getOppositeModelId().intValue();
                int paramOppositeInstanceId = param.getOppositeInstanceId().intValue();

                int aParamOwnModelId = aParam.getOwnModelId().intValue();
                int aParamOwnInstanceId = aParam.getOwnInstanceId().intValue();
                int aParamOppositeModelId = aParam.getOppositeModelId().intValue();
                int aParamOppositeInstanceId = aParam.getOppositeInstanceId().intValue();
                if (isAdd) {
                    //数据库存储数据为双向存储，把本体模型和对端模型当成两条数据插入，获取对端数据，并去除和本体关联的数据，此条件避免进入死循环，
                    if (((paramOwnModelId == aParamOppositeModelId) && (paramOwnInstanceId == aParamOppositeInstanceId)) &&
                            ((paramOppositeModelId == aParamOwnModelId) && (paramOppositeInstanceId == aParamOwnInstanceId))) {
                        //加入对端数据，但不进入递归循环
                        addList.add(param);
                    }
                }
                //获取所有的关联数据，递归查询
                if (((paramOwnModelId == aParamOppositeModelId) && (paramOwnInstanceId == aParamOppositeInstanceId)) &&
                        ((paramOppositeModelId != aParamOwnModelId) && (paramOppositeInstanceId != aParamOwnInstanceId))) {
                    addList.add(param);
                    getToPOInfoByChild(param, topoAllInfo, addList, isAdd);
                }
            }
        } catch (Throwable e) {
            log.error("fail to addInstanceToPo param{}, case by {}", "", e, e.getMessage());
        }
    }

    @Override
    public Reply viewInstanceToPo(QueryInstanceRelationToPoParam param) {
        InstanceTopoView instanceTopoView = new InstanceTopoView();
        LastData lastData = new LastData();
        //找到根节点,并获取实例拓扑配置信息
        try {
            //初次打开页面,显示实例拓扑
            if (null == param.getLastData().getNodes()) {
                initInstanceTopo(param, instanceTopoView);
            }

            //判断对端模型可有数据
            if (StringUtils.isNotEmpty(param.getAction())) {
                //实例拓扑操作
                //新增模型拓扑框
                InstanceActionType actionType = InstanceActionType.valueOf(param.getAction());
                switch (actionType) {
                    case addModel:
                        lastData = doAddNewModel(param);
                        break;
                    case addInstance:
                        lastData = doAddNewInstance(param);
                        break;
                    case addLine:
                        lastData = doAddNewLine(param);
                }

                HideModelDataView hideModelDataView = new HideModelDataView();
                hideModelDataView.setHide(param.getHideModelIds());
                hideModelDataView.setShow(param.getShowModelIds());
                instanceTopoView.setHideModelData(hideModelDataView);
                instanceTopoView.setIntanceViewId(param.getInstanceViewId());

                instanceTopoView.setData(lastData);
                instanceTopoView.setLastData(lastData);
            }

            return Reply.ok(instanceTopoView);
        } catch (Exception e) {
            log.error("viewInstanceToPo {}", e);
        }

        return null;
    }

    //查询实例拓扑数据,并按照左到右,上到下的顺序,对点,边,组对应排序
    private void initInstanceTopo(QueryInstanceRelationToPoParam param, InstanceTopoView instanceTopoView) throws Exception {
        QueryInstanceRelationsParam qparam = param.getOwmRelationsParam();

        if (null != qparam) {
            List<Integer> instanceIds = qparam.getInstanceIds();
            if (null != instanceIds && instanceIds.size() > 0) {
                //查询实例视图
                Map criteria = new HashMap();
                criteria.put("modelInstanceId", instanceIds.get(0));
                List<ModelInstanceDto> instanceDtos = mwModelManageDao.selectModelInstance(criteria);

                List<Integer> allInstanceIds = new ArrayList<>();
                Integer instanceId = instanceIds.get(0);
                allInstanceIds.add(instanceId);
                Session session = connectionPool.getSession();

                //遍历拓扑关系,构造前端显示的数据结构
                //根据模型id查询模型关系的层数
                int level = ModelAssetUtils.findModelRelateLevel(session, qparam.getModelId());
                List<NodeParam> nodeParams = new ArrayList<>();
                NodeParam rootNodeParam = new NodeParam(qparam.getModelId(), instanceId);
                rootNodeParam.setLevel(0);

                //查询所有关系信息,根据模型id获取每个关系对应的名称
                ModelAsset modelAsset = session.load(ModelAsset.class, qparam.getModelId(), -1);
                Map<Integer, ModelRelationDTO> modelAssetMap = new HashMap<>();
                Set<Long> visitedRelation = new HashSet<>();
                Set<ModelConn> modelConnSet = new HashSet<>();
                if (null != modelAsset) {
                    doVisitModelAsset(modelAsset, modelAssetMap, visitedRelation, modelConnSet);
                }

                //根据模型关系层数查找拓扑联系,并根据模型关系过滤连线
                //默认设置默认空间,如果选择了视图,则设置实例视图空间
                String space = ModelAssetUtils.INSTANCE_PRE + instanceIds.get(0);
                if (null != param.getInstanceViewId() && param.getInstanceViewId() > 0) {
                    space = ModelAssetUtils.INSTANCE_VIEW_SPACE + param.getInstanceViewId();
                }

                List<EdgeParam> edgeParams = new ArrayList<>();
                Map<String, List<EdgeParam>> edgeMap = new HashMap<>();
                //处理手动编辑实例拓扑的场景,增加实例id标签的边
                addEdgeParams(session, edgeParams, edgeMap, rootNodeParam, space, level, modelConnSet, allInstanceIds);

                //处理数据同步自动添加关系的场景,增加虚拟标签的边
                if (ConnectCheckModelEnum.CITRIXADC.getModelId().equals(qparam.getModelId())) {
                    addEdgeParams(session, edgeParams, edgeMap, rootNodeParam, ModelAssetUtils.CITRIX_SPACE + instanceId, level, modelConnSet, allInstanceIds);
                } else {
                    addEdgeParams(session, edgeParams, edgeMap, rootNodeParam, ModelAssetUtils.VIRTUAL_SPACE, level, modelConnSet, allInstanceIds);
                }

                //由于虚拟化等数据同步时,并不会保存实例拓扑信息
                //判断是否保存过实例拓扑,如果保存过,则获取点和分组信息,然后把neo4j的边按顺序加入
                boolean hasTopoInfo = false;
                if (null != instanceDtos && instanceDtos.size() > 0) {
                    ModelInstanceDto modelInstanceDto = instanceDtos.get(0);
                    ModelInstanceTopoInfo modelInstanceTopoInfo = modelInstanceDto.getTopoInfo();
                    if (null != modelInstanceTopoInfo) {
                        instanceTopoView.extractFrom(modelInstanceTopoInfo);
                        instanceTopoView.addEdges(edgeParams);
                        hasTopoInfo = true;
                    }
                }

                if (!hasTopoInfo) {
                    //获取点集合,并对边进行排序,并设置level
                    nodeParams.add(rootNodeParam);
                    edgeParams = ModelUtils.doFillNodeParamsAndSortEdge(rootNodeParam, nodeParams, edgeMap);

                    //获取实例信息,重新设置节点及边的数据
                    Map<Integer, InstanceModelMapper> instanceMap = doGetInstanceModelMap(allInstanceIds);
                    Set<Integer> modelIds = new HashSet<>();
                    for (InstanceModelMapper mapper : instanceMap.values()) {
                        modelIds.add(mapper.getModelId());
                    }

                    //检查是否有无实例节点,如果有则重新初始化
                    for (NodeParam nodeParam : nodeParams) {
                        if (nodeParam.isEmptyNode()) {
                            modelIds.add(nodeParam.getComboId());
                            nodeParam.initEmptyNode(nodeParam.getComboId());
                        }
                    }

                    //获取模型名称
                    Map<Integer, String> modelNameMap = new HashMap<>();
                    List<ModelInfo> modelInfoList = mwModelManageDao.selectModelListByIds(new ArrayList<>(modelIds));
                    if (modelIds.size() > 0) {
                        for (ModelInfo modelInfo : modelInfoList) {
                            modelNameMap.put(modelInfo.getModelId(), modelInfo.getModelName());
                        }
                    }

                    Map<Integer, ComboParam> comboParamMap = new HashMap<>();
                    if (modelIds.size() > 0) {
                        for (ModelInfo modelInfo : modelInfoList) {
                            ComboParam comboParam = new ComboParam();
                            comboParam.setId(modelInfo.getModelId());
                            comboParam.extractFrom(modelAssetMap);
                            comboParamMap.put(comboParam.getId(), comboParam);
                        }
                    }

                    //遍历点和线重新设置信息,并根据节点的顺序排列ComboParam
                    for (NodeParam nodeParam : nodeParams) {
                        nodeParam.extractInfoFrom(instanceMap);
                    }

                    List<ComboParam> comboParams = ModelUtils.sortComboParams(nodeParams, comboParamMap);
                    LastData lastData = new LastData();
                    lastData.setNodes(nodeParams);
                    lastData.setEdges(edgeParams);
                    lastData.setCombos(comboParams);
                    instanceTopoView.setLastData(lastData);
                    instanceTopoView.setData(lastData);
                    instanceTopoView.initHideModelDataView();
                }
            }
        }
    }

    private void addEdgeParams(Session session, List<EdgeParam> edgeParams, Map<String, List<EdgeParam>> edgeMap, NodeParam start, String space
            , int level, Set<ModelConn> modelConnSet, List<Integer> allInstanceIds) {
        List<EdgeParam> findEdgeParams = ModelAssetUtils.findEdgeBySpace(session, start, space, level);
        for (EdgeParam edgeParam : findEdgeParams) {
            ModelConn modelConn = new ModelConn(edgeParam);
            if (!modelConnSet.contains(modelConn)) {
                continue;
            }
            if (!edgeParams.contains(edgeParam)) {
                edgeParams.add(edgeParam);
                doAddNodeParam(new NodeParam(edgeParam.getSource()), allInstanceIds);
                doAddNodeParam(new NodeParam(edgeParam.getTarget()), allInstanceIds);

                List<EdgeParam> list = edgeMap.get(edgeParam.getSource());
                if (null == list) {
                    list = new ArrayList<>();
                    edgeMap.put(edgeParam.getSource(), list);
                }
                list.add(edgeParam);
            }
        }
    }

    private void doAddNodeParam(NodeParam nodeParam, List<Integer> allInstanceIds) {
        if (!allInstanceIds.contains(nodeParam.getRealId())) {
            allInstanceIds.add(nodeParam.getRealId());
        }
    }

    private Map<Integer, InstanceModelMapper> doGetInstanceModelMap(List<Integer> allInstanceIds) {
        Map<Integer, InstanceModelMapper> instanceMap = new HashMap<>();

        if (null != allInstanceIds && allInstanceIds.size() > 0) {
            List<Map> instanceNameList = mwModelRelationsDao.getInstanceNameByIds(allInstanceIds);
            Set<Integer> modelIds = new HashSet<>();
            for (Map data : instanceNameList) {
                InstanceModelMapper instanceModelMapper = InstanceModelMapper.builder()
                        .instanceId(Integer.parseInt(data.get("instanceId").toString()))
                        .instanceName(data.get("instanceName").toString())
                        .modelId(Integer.parseInt(data.get("modelId").toString()))
                        .build();
                instanceMap.put(instanceModelMapper.getInstanceId(), instanceModelMapper);
                modelIds.add(instanceModelMapper.getModelId());
            }
        }
        return instanceMap;
    }

    private LastData doAddNewModel(QueryInstanceRelationToPoParam param) {
        LastData lastData = param.getLastData();
        List<NodeParam> nodeParams = lastData.getNodes();
        List<ComboParam> comboParams = lastData.getCombos();

        //需要新增一个无实例数据
        NodeParam nodeParam = new NodeParam();
        nodeParam.initEmptyNode(param.getOppoRelationsParamList().get(0).getModelId());

        //找到节点插入位置
        QueryInstanceRelationsParam own = param.getOwmRelationsParam();
        NodeParam start = new NodeParam(own.getModelId(), own.getInstanceIds().get(0));
        NodePosInfo pos = findAddModelPos(start, lastData);
        if (0 == pos.getPos()) {
            nodeParams.add(nodeParam);
        } else {
            nodeParams.add(pos.getPos(), nodeParam);
        }
        nodeParam.setLevel(pos.getLevel() + 1);

        QueryInstanceRelationsParam qparam = param.getOppoRelationsParamList().get(0);
        ComboParam comboParam = new ComboParam(qparam.getModelId(), qparam.getModelName());
        if (0 == pos.getComboPos()) {
            comboParams.add(comboParam);
        } else {
            comboParams.add(pos.getComboPos(), comboParam);
        }
        return lastData;
    }


    //新增模型的实例节点放在下一个级别的后面
    private NodePosInfo findAddModelPos(NodeParam nodeParam, LastData data) {
        NodePosInfo nodePosInfo = new NodePosInfo();
        if (null != data.getNodes()) {
            int level = 0;
            int nextLevel = 99;
            boolean find = false;

            NodeParam node = null;
            for (int i = 0; i < data.getNodes().size(); i++) {
                NodeParam cnode = data.getNodes().get(i);
                //确定新增节点的层级的前一级
                if (nodeParam.equals(cnode)) {
                    level = cnode.getLevel();
                    nextLevel = level + 1;
                    find = true;
                }

                if (find && cnode.getLevel() > nextLevel) {
                    nodePosInfo.setPos(i);
                    node = cnode;
                    break;
                }
            }

            if (null != node) {
                ComboParam combo = new ComboParam(node.getComboId(), node.getLabel());
                for (int i = 0; i < data.getCombos().size(); i++) {
                    ComboParam comboParam = data.getCombos().get(i);
                    if (combo.equals(comboParam)) {
                        nodePosInfo.setComboPos(i);
                    }
                }
            }

            nodePosInfo.setLevel(level);
        }
        return nodePosInfo;
    }

    private LastData doAddNewInstance(QueryInstanceRelationToPoParam param) {
        QueryInstanceRelationsParam qparam = param.getOppoRelationsParamList().get(0);
        LastData lastData = param.getLastData();
        List<NodeParam> nodeParams = lastData.getNodes();

        List<Integer> instanceIds = qparam.getInstanceIds();
        List<Integer> allInstanceIds = new ArrayList<>();
        allInstanceIds.addAll(instanceIds);

        //获取实例信息
        Map<Integer, InstanceModelMapper> instanceModelMap = doGetInstanceModelMap(allInstanceIds);

        for (Integer instanceId : qparam.getInstanceIds()) {
            NodeParam nodeParam = new NodeParam();
            nodeParam.setRealId(instanceId);
            nodeParam.extractInfoFrom(instanceModelMap);
            NodePosInfo pos = findAddInstancePos(nodeParam, lastData);
            nodeParams.add(pos.getPos(), nodeParam);
            nodeParam.setLevel(pos.getLevel());
        }

        //删除nodes中的“无实例”实例数据
        Iterator<NodeParam> it = lastData.getNodes().iterator();
        while (it.hasNext()) {
            NodeParam m = it.next();
            if (qparam.getModelId().equals(m.getComboId())
                    && NodeParam.EMPTY_LABEL.equals(m.getLabel())) {
                it.remove();
                break;
            }
        }

        return lastData;
    }

    private LastData doAddNewLine(QueryInstanceRelationToPoParam param) {
        LastData lastData = param.getLastData();
        QueryInstanceRelationsParam sourceParam = param.getOwmRelationsParam();
        List<List<Integer>> targetNodes = param.getTargetNodes();

        List<SortNewEdge> sortNewEdges = new ArrayList<>();
        if (null != sourceParam && null != targetNodes) {
            NodeParam start = new NodeParam(sourceParam.getModelId(), sourceParam.getInstanceIds().get(0));
            for (List<Integer> targetNodeInfo : targetNodes) {
                NodeParam end = new NodeParam(targetNodeInfo.get(0), targetNodeInfo.get(1));
                EdgeParam edgeParam = new EdgeParam(start, end);
                SortNewEdge sortNewEdge = new SortNewEdge(edgeParam);
                sortNewEdges.add(sortNewEdge);
            }

            List<EdgeParam> edgeParams = lastData.getEdges();
            if (null != edgeParams) {
                for (EdgeParam edgeParam : edgeParams) {
                    SortNewEdge sortNewEdge = new SortNewEdge(edgeParam);
                    sortNewEdges.add(sortNewEdge);
                }
            }

            Map<String, SortNode> sortNodeMap = new HashMap<>();
            for (int i = 0; i < lastData.getNodes().size(); i++) {
                NodeParam nodeParam = lastData.getNodes().get(i);
                SortNode sortNode = new SortNode(nodeParam, i);
                sortNodeMap.put(nodeParam.getId(), sortNode);
            }

            for (SortNewEdge sortNewEdge : sortNewEdges) {
                SortNode startSortNode = sortNodeMap.get(sortNewEdge.getEdgeParam().getSource());
                SortNode endSortNode = sortNodeMap.get(sortNewEdge.getEdgeParam().getTarget());
                if (null != startSortNode && null != endSortNode) {
                    sortNewEdge.setStartIndex(startSortNode.getIndex());
                    sortNewEdge.setEndIndex(endSortNode.getIndex());
                    ;
                }
            }

            Collections.sort(sortNewEdges);
            List<EdgeParam> newEdgeParams = sortNewEdges.stream().map(SortNewEdge::getEdgeParam).collect(Collectors.toList());

            if (newEdgeParams.size() > 0) {
                lastData.setEdges(newEdgeParams);
            }
        }

        return lastData;
    }

    private NodePosInfo findAddInstancePos(NodeParam nodeParam, LastData data) {
        NodePosInfo nodePosInfo = new NodePosInfo();
        //找到相同combo的节点
        int startIndex = -1;
        int level = 0;
        int lastNodePos = data.getNodes().size() - 1;
        for (int i = 0; i < data.getNodes().size(); i++) {
            NodeParam node = data.getNodes().get(i);
            if (node.getComboId().equals(nodeParam.getComboId())) {
                startIndex = i;
                level = node.getLevel();
            }

            if (startIndex > 0) {
                if (!node.getComboId().equals(nodeParam.getComboId())) {
                    nodePosInfo.setPos(i);
                    nodePosInfo.setLevel(level);
                    break;
                }

                if (i == lastNodePos) {
                    nodePosInfo.setPos(i + 1);
                    nodePosInfo.setLevel(level);
                }
            }
        }

        return nodePosInfo;
    }

    private void doVisitInstanceAsset(InstanceAsset instanceAsset
            , List<Integer> allInstanceIds, List<NodeParam> nodeParams, List<EdgeParam> edgeParams, Set<Integer> visitedInstanceSet) {
        Integer instanceId = instanceAsset.getId();
        if (visitedInstanceSet.contains(instanceId)) {
            return;
        }
        visitedInstanceSet.add(instanceId);
        allInstanceIds.add(instanceAsset.getId());
        NodeParam nodeParam = new NodeParam();
        nodeParam.setRealId(instanceAsset.getId());

        nodeParams.add(nodeParam);
        if (null != instanceAsset.getInstanceRelates()) {
            for (InstanceRelate instanceRelate : instanceAsset.getInstanceRelates()) {
                EdgeParam edgeParam = new EdgeParam();
                edgeParam.setSource(String.valueOf(instanceRelate.getStartNode().getId()));
                edgeParam.setTarget(String.valueOf(instanceRelate.getEndNode().getId()));
                edgeParams.add(edgeParam);

                doVisitInstanceAsset(instanceRelate.getEndNode(), allInstanceIds, nodeParams, edgeParams, visitedInstanceSet);
            }
        }
    }

    private void doVisitModelAsset(ModelAsset modelAsset, Map<Integer, ModelRelationDTO> modelAssetMap
            , Set<Long> visitedRelation, Set<ModelConn> modelConnSet) {
        if (null == modelAsset.getModelRelates()) {
            return;
        }

        for (ModelRelate modelRelate : modelAsset.getModelRelates()) {
            if (visitedRelation.contains(modelRelate.getRelationId())) {
                continue;
            }
            ModelConn modelConn = new ModelConn(modelRelate.getStartNode().getId(), modelRelate.getEndNode().getId());
            modelConnSet.add(modelConn);
            visitedRelation.add(modelRelate.getRelationId());
            addModelRelationDTO(modelRelate.getStartNode(), modelRelate, modelAssetMap);
            addModelRelationDTO(modelRelate.getEndNode(), modelRelate, modelAssetMap);
            doVisitModelAsset(modelRelate.getEndNode(), modelAssetMap, visitedRelation, modelConnSet);
        }
    }

    private void addModelRelationDTO(ModelAsset modelAsset, ModelRelate modelRelate, Map<Integer, ModelRelationDTO> modelAssetMap) {
        ModelRelationDTO modelRelationDTO = modelAssetMap.get(modelAsset.getId());
        if (null == modelRelationDTO) {
            modelRelationDTO = modelRelate.getModelRelationDTO();
            modelAssetMap.put(modelAsset.getId(), modelRelationDTO);
        }
    }

    @Override
    public Reply queryRelationNumInstanceToPo(QueryInstanceRelationToPoParam param) {
        try {
            RelationNumInstanceTopoView ret = new RelationNumInstanceTopoView();
            if (param.getOppoRelationsParamList() != null && param.getOppoRelationsParamList().size() > 0) {
                List<EdgeParam> edgesLastInfo = new ArrayList<>();
                if (param.getLastData() != null) {
                    LastData lastDataAll = param.getLastData();
                    edgesLastInfo = lastDataAll.getEdges();
                }
                //默认所有模型只有一个上级
                for (QueryInstanceRelationsParam qparam : param.getOppoRelationsParamList()) {
                    List<String> sourceList = new ArrayList<>();
                    //由于前端新增实例数据时，获取不了 关联的模型Id和实例id
                    //根据参数relationInstanceList中的新增实例的modelId，去lastData的edges中获取source
                    for (EdgeParam edge : edgesLastInfo) {
                        //循环获取
                        String[] targetStr = edge.getTarget().split(EdgeParam.SEP);
                        if (targetStr.length > 1 && targetStr[0].equals(qparam.getModelId() + "")) {
                            String lastModelId = edge.getSource().split(EdgeParam.SEP)[0];
                            sourceList.add(lastModelId);
                        }
                    }
                    List<String> distinctList = sourceList.stream().distinct().collect(Collectors.toList());
                    if (distinctList != null && distinctList.size() > 0) {
                        Integer ownModelId = Integer.valueOf(distinctList.get(0));
                        Session session = connectionPool.getSession();
                        ModelAsset modelAsset = session.load(ModelAsset.class, ownModelId, 1);
                        ret.setOwnModelId(ownModelId);
                        ret.setOppositeModelId(qparam.getModelId());
                        ret.extractFrom(modelAsset);
                    }
                }
            }
            return Reply.ok(ret);
        } catch (Exception e) {
            log.error("fail to queryRelationNumInstanceToPo param{}, case by {}", "", e);
            return Reply.fail(500, "新增拓扑实例查询关联数量失败");
        }
    }

    @Override
    public Reply deleteInstanceToPo(QueryInstanceRelationToPoParam param) {
        List<NodeParam> nodesInfo = new ArrayList<>();
        List<EdgeParam> edgesInfo = new ArrayList<>();
        List<ComboParam> combosInfo = new ArrayList<>();
        try {
            if (param.getOppoRelationsParamList() != null && param.getOppoRelationsParamList().size() > 0) {
                List<NodeParam> nodesLastInfo = new ArrayList<>();
                List<EdgeParam> edgesLastInfo = new ArrayList<>();
                List<ComboParam> combosLastInfo = new ArrayList<>();
                if (param.getLastData() != null) {
                    LastData lastDataAll = param.getLastData();
                    nodesLastInfo = lastDataAll.getNodes();
                    edgesLastInfo = lastDataAll.getEdges();
                    combosLastInfo = lastDataAll.getCombos();
                }
                //默认所有模型只有一个上级
                for (QueryInstanceRelationsParam qparam : param.getOppoRelationsParamList()) {
                    //删除模型
                    if (param.getIsDeleteModel()) {
                        doDelModel(qparam, nodesLastInfo, edgesLastInfo, combosLastInfo);
                    } else {
                        //删除实例
                        doDelInstance(qparam, nodesLastInfo, edgesLastInfo, combosLastInfo, nodesInfo, edgesInfo);
                    }
                }
                nodesInfo.addAll(nodesLastInfo);
                edgesInfo.addAll(edgesLastInfo);
                combosInfo.addAll(combosLastInfo);
            }
            Comparator<NodeParam> comparatorNode = Comparator.comparing(NodeParam::getLevel).thenComparing(NodeParam::getComboId);
            List<NodeParam> nodesInfos = nodesInfo.stream().sorted(comparatorNode).collect(Collectors.toList());

            InstanceTopoView instanceTopoView = new InstanceTopoView();
            LastData lastData = new LastData();
            lastData.setNodes(nodesInfos);

            Comparator<EdgeParam> comparatorEdge = Comparator.comparing(EdgeParam::getSource).thenComparing(EdgeParam::getTarget);
            List<EdgeParam> edgesInfos = edgesInfo.stream().sorted(comparatorEdge).collect(Collectors.toList());
            lastData.setEdges(edgesInfos);
            lastData.setCombos(combosInfo);

            instanceTopoView.setData(lastData);
            instanceTopoView.setLastData(lastData);
            return Reply.ok(instanceTopoView);
        } catch (Exception e) {
            log.error("fail to deleteInstanceToPo param{}, case by {}", "", e);
            return Reply.fail(500, "删除拓扑实例失败");
        }
    }

    private void doDelModel(QueryInstanceRelationsParam qparam, List<NodeParam> nodesLastInfo, List<EdgeParam> edgesLastInfo, List<ComboParam> combosLastInfo) {
        Iterator<NodeParam> nodeIts = nodesLastInfo.iterator();
        while (nodeIts.hasNext()) {
            NodeParam s = nodeIts.next();
            if ((qparam.getModelId()).equals(s.getComboId())) {
                nodeIts.remove();
            }
        }
        Iterator<EdgeParam> edgeIts = edgesLastInfo.iterator();
        while (edgeIts.hasNext()) {
            EdgeParam s = edgeIts.next();

            String[] sourceStr = s.getSource().split(EdgeParam.SEP);
            if (sourceStr[0].equals(qparam.getModelId().toString())) {
                edgeIts.remove();
            }

            String[] targetStr = s.getTarget().split(EdgeParam.SEP);
            if (targetStr[0].equals(qparam.getModelId().toString())) {
                edgeIts.remove();
            }
        }
        Iterator<ComboParam> combosIts = combosLastInfo.iterator();
        while (combosIts.hasNext()) {
            ComboParam s = combosIts.next();
            if (s.getId().equals(qparam.getModelId())) {
                combosIts.remove();
            }
        }
    }

    private void doDelInstance(QueryInstanceRelationsParam qparam, List<NodeParam> nodesLastInfo, List<EdgeParam> edgesLastInfo
            , List<ComboParam> combosLastInfo, List<NodeParam> nodesInfo, List<EdgeParam> edgesInfo) {

        for (Integer instanceId : qparam.getInstanceIds()) {
            Iterator<NodeParam> nodeIts = nodesLastInfo.iterator();
            while (nodeIts.hasNext()) {
                NodeParam s = nodeIts.next();
                if ((qparam.getModelId()).equals(s.getComboId()) && instanceId.equals(s.getRealId())) {
                    nodeIts.remove();
                }
            }

            Iterator<EdgeParam> edgeIts = edgesLastInfo.iterator();

            String delTarget = qparam.getModelId() + EdgeParam.SEP + instanceId;
            List<String> sourceList = new ArrayList<>();
            List<String> targetList = new ArrayList<>();

            //删除与点有关的边
            while (edgeIts.hasNext()) {
                EdgeParam s = edgeIts.next();
                if (delTarget.equals(s.getTarget())) {
                    sourceList.add(s.getSource());
                    edgeIts.remove();

                }

                if (delTarget.equals(s.getSource())) {
                    targetList.add(s.getTarget());
                    edgeIts.remove();
                }
            }

            //实例删除后，判断该模型下是否还有实例，没有，则新增一条“无实例数据”
            int nodesNum = 0;
            for (NodeParam m : nodesLastInfo) {
                if ((qparam.getModelId()).equals(m.getComboId())) {
                    nodesNum++;
                }
            }
            if (nodesNum == 0) {
                NodeParam emptyNode = new NodeParam();
                emptyNode.initEmptyNode(qparam.getModelId());
                nodesInfo.add(emptyNode);
            }

            //实例删除后，判断该模型下是否还有实例，没有，则新增一条“无实例”的连接线
            //该“无实例”需要连接上级模型和下级模型
            if (nodesNum == 0) {
                for (String source : sourceList) {
                    NodeParam src = new NodeParam();
                    src.setId(source);
                    NodeParam dest = new NodeParam();
                    dest.initEmptyNode(qparam.getModelId());

                    EdgeParam edgeParam = new EdgeParam(src, dest);
                    edgesInfo.add(edgeParam);
                }

                for (String target : targetList) {
                    NodeParam src = new NodeParam();
                    src.initEmptyNode(qparam.getModelId());
                    NodeParam dest = new NodeParam();
                    dest.setId(target);

                    EdgeParam edgeParam = new EdgeParam(src, dest);
                    edgesInfo.add(edgeParam);
                }
            }
        }
    }

    @Override
    public Reply hideModelToPo(QueryInstanceRelationToPoParam param) {
        //原始数据
        List<Map> nodesInfo = new ArrayList<>();
        List<Map> edgesInfo = new ArrayList<>();
        List<Map> combosInfo = new ArrayList<>();
        //过滤隐藏后的数据
        List<Map> nodesFilterInfo = new ArrayList<>();
        List<Map> edgesFilterInfo = new ArrayList<>();
        List<Map> combosFilterInfo = new ArrayList<>();


        List<Integer> hideModelIdList = new ArrayList<>();
        List<Integer> showModelIdList = new ArrayList<>();
        List<Integer> hideModelIds = new ArrayList<>();
        List<Integer> showModelIds = new ArrayList<>();
        Map dataMap = new HashMap();
        Map FilterMap = new HashMap();
        try {
            if (param.getHideModelIds() != null && param.getHideModelIds().size() > 0) {
                hideModelIds.addAll(param.getHideModelIds());
                hideModelIdList.addAll(param.getHideModelIds());
            }
            if (param.getShowModelIds() != null && param.getShowModelIds().size() > 0) {
                showModelIds.addAll(param.getShowModelIds());
                showModelIdList.addAll(param.getShowModelIds());
            }

            if (param.getHideModelToPoListParam() != null) {
                QueryHideModelToPo hideParam = param.getHideModelToPoListParam();
                //1：显示，0：隐藏
                if (hideParam.getType() == 1) {
                    //显示，删除数据
                    showModelIds.addAll(hideParam.getModelIds());
                    showModelIdList.addAll(hideParam.getModelIds());
                }
                if (hideParam.getType() == 0) {
                    //隐藏，新增数据
                    hideModelIds.addAll(hideParam.getModelIds());
                    hideModelIdList.addAll(hideParam.getModelIds());
                }
            }
            //隐藏的模型ids去除显示的modelIds  余下的就要最终要隐藏的
            hideModelIds.removeAll(showModelIds);
            //显示的模型ids去除的隐藏modelIds  余下的就要最终要显示的
            showModelIdList.removeAll(hideModelIdList);
            List<Map> newList = new ArrayList<>();
            if (param.getLastData() != null && ((Map) param.getLastData()).size() != 0) {
                if (param.getLastData() != null) {
                    Map<String, List> lastDataAll = (Map) param.getLastData();
                    nodesFilterInfo.addAll(lastDataAll.get("nodes"));
                    edgesFilterInfo.addAll(lastDataAll.get("edges"));
                    combosFilterInfo.addAll(lastDataAll.get("combos"));

                    nodesInfo.addAll(lastDataAll.get("nodes"));
                    edgesInfo.addAll(lastDataAll.get("edges"));
                    combosInfo.addAll(lastDataAll.get("combos"));
                    Iterator<Map> edges = edgesFilterInfo.iterator();
                    Set<Integer> set = new HashSet();
                    //对前端页面展示的数据进行隐藏过滤处理
                    while (edges.hasNext()) {
                        Map m = edges.next();
                        String target = m.get("target").toString();
                        String[] targetStr = target.split("_");
                        Integer targetInt = Integer.valueOf(targetStr[0]);
                        String source = m.get("source").toString();
                        String[] sourceStr = source.split("_");
                        Integer sourceInt = Integer.valueOf(sourceStr[0]);
                        //去除edges数据
                        if (hideModelIds.size() > 0 && hideModelIds.contains(targetInt)) {
                            newList.add(m);
                            edges.remove();
                            set.add(targetInt);
                        }
                    }
                    Iterator<Map> edges1 = edgesFilterInfo.iterator();
                    //对前端页面展示的数据进行隐藏过滤处理
                    while (edges1.hasNext()) {
                        Map m = edges1.next();
                        String target = m.get("target").toString();
                        String[] targetStr = target.split("_");
                        Integer targetInt = Integer.valueOf(targetStr[0]);
                        String source = m.get("source").toString();
                        String[] sourceStr = source.split("_");
                        Integer sourceInt = Integer.valueOf(sourceStr[0]);
                        if (hideModelIds.size() > 0 && hideModelIds.contains(sourceInt)) {
                            set.add(targetInt);
                            //edges数据具有线性关系，通过匹配source获取target数据，在通过target匹配下一个的source，一直递归下去。
                            newList.add(m);
                            getHideModelInfo(set, newList, targetInt, edgesFilterInfo);
                        }
                    }
                    edgesFilterInfo.removeAll(newList);
                    //去除nodes数据
                    for (Integer modelId : set) {
                        Iterator<Map> nodes = nodesFilterInfo.iterator();
                        while (nodes.hasNext()) {
                            Map m = nodes.next();
                            if (modelId.intValue() == Integer.valueOf(m.get("comboId").toString()).intValue()) {
                                nodes.remove();
                            }
                        }
                    }
                    //去除combos数据
                    for (Integer modelId : set) {
                        Iterator<Map> combos = combosFilterInfo.iterator();
                        while (combos.hasNext()) {
                            Map m = combos.next();
                            if (modelId.intValue() == (Integer.valueOf(m.get("id").toString())).intValue()) {
                                combos.remove();
                            }
                        }
                    }
                }
            }
            Map dataAll = new HashMap();

            //对实例数据排序，保存level==0的（本体数据）排在最前面，所有combosId数据排在一起
            List<Map> nodesInfos = nodesInfo.stream().sorted(Comparator.comparing(s -> Integer.valueOf(s.get("comboId").toString())))
                    .sorted(Comparator.comparing(s -> Integer.valueOf(s.get("level").toString()))).collect(Collectors.toList());
            dataMap.put("nodes", nodesInfos);
            List<Map> edgesInfos = edgesInfo.stream().sorted(Comparator.comparing(s -> s.get("target").toString()))
                    .sorted(Comparator.comparing(s -> s.get("source").toString())).collect(Collectors.toList());
            dataMap.put("edges", edgesInfos);
            dataMap.put("combos", combosInfo);

            //对实例数据排序，保存level==0的（本体数据）排在最前面，所有combosId数据排在一起
            List<Map> nodesFilterInfos = nodesFilterInfo.stream().sorted(Comparator.comparing(s -> Integer.valueOf(s.get("comboId").toString())))
                    .sorted(Comparator.comparing(s -> Integer.valueOf(s.get("level").toString()))).collect(Collectors.toList());
            List<Map> edgesFilterInfos = edgesFilterInfo.stream().sorted(Comparator.comparing(s -> s.get("target").toString()))
                    .sorted(Comparator.comparing(s -> s.get("source").toString())).collect(Collectors.toList());
            FilterMap.put("nodes", nodesFilterInfos);
            FilterMap.put("edges", edgesFilterInfos);
            FilterMap.put("combos", combosFilterInfo);

            Map hideMap = new HashMap();
            hideMap.put("show", showModelIdList);
            hideMap.put("hide", hideModelIds);
            dataAll.put("lastData", dataMap);
            dataAll.put("data", FilterMap);
            dataAll.put("hideModelData", hideMap);
            return Reply.ok(dataAll);
        } catch (Exception e) {
            log.error("fail to hideModelToPo param{}, case by {}", "", e);
            return Reply.fail(500, "显示/隐藏关联模型失败");
        }
    }

    private void getHideModelInfo(Set<Integer> set, List<Map> newList, Integer target, List<Map> sourceMapInfo) {
        try {
            Iterator<Map> edges = sourceMapInfo.iterator();
            //对前端页面展示的数据进行隐藏过滤处理
            while (edges.hasNext()) {
                Map m = edges.next();
                Integer sourceInt = Integer.valueOf(m.get("source").toString().split("_")[0]);
                Integer targetInt = Integer.valueOf(m.get("target").toString().split("_")[0]);
                if (target.intValue() == sourceInt.intValue()) {
                    set.add(targetInt);
                    newList.add(m);
                    getHideModelInfo(set, newList, targetInt, sourceMapInfo);
                }
            }
        } catch (Throwable e) {
            log.error("fail to getHideModelInfo param{}, case by {}", "", e);
            throw e;
        }
    }

    /**
     * 获取已关联的模型列表
     *
     * @param param
     * @return
     */
    @Override
    public Reply getTOPOModel(QueryInstanceRelationToPoParam param) {
        try {
            List<Map> mapList = new ArrayList<>();
            if (param.getOwmRelationsParam() != null) {
                QueryInstanceRelationsParam qparam = param.getOwmRelationsParam();
                List<Map> edgesLastInfo = new ArrayList<>();
                List<Map> combosLastInfo = new ArrayList<>();
                if (param.getLastData() != null) {
                    Map<String, List> lastDataAll = (Map) param.getLastData();
                    edgesLastInfo = lastDataAll.get("edges");
                    combosLastInfo = lastDataAll.get("combos");
                }
                mapList = mwModelInstanceDao.getModelRelationInfo(null, qparam.getModelId());
                //默认所有模型只有一个上级
                List<String> sourceList = new ArrayList<>();
                //由于前端新增实例数据时，获取不了 关联的模型Id和实例id
                //根据参数relationInstanceList中的新增实例的modelId，去lastData的edges中获取source
                for (Map edge : edgesLastInfo) {
                    //循环获取
                    String target = edge.get("target").toString();
                    String[] targetStr = target.split("_");
                    if (targetStr.length > 1 && targetStr[0].equals(qparam.getModelId() + "")) {
                        String source = edge.get("source").toString();
                        String lastModelId = source.split("_")[0];
                        sourceList.add(lastModelId);
                    }
                }
                //对combos数据去重
                List<Map> modelIdDistinctList = combosLastInfo.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(s -> s.get("id").toString()))), ArrayList::new));
                List<String> modelIds = new ArrayList<>();
                //获取所有已存在的模型id
                for (Map m : modelIdDistinctList) {
                    modelIds.add(m.get("id").toString());
                }
                List<String> distinctList = sourceList.stream().distinct().collect(Collectors.toList());
                if (distinctList != null && distinctList.size() > 0) {
                    mapList = mwModelInstanceDao.getModelRelationInfo(Integer.valueOf(distinctList.get(0)), qparam.getModelId());
                }
            }
            return Reply.ok(mapList);
        } catch (Exception e) {
            log.error("fail to getTOPOModel param{}, case by {}", e);
            return Reply.fail(500, "根据模型id获取所有模型关系关联数据");
        }
    }

    @Override
    public void deleteRelationByInstances(Integer modelId, List<Integer> instanceIds) {
        try {
            Session session = connectionPool.getSession();
            List<InstanceNode> list = new ArrayList<>();
            for (Integer id : instanceIds) {
                NodeParam nodeParam = new NodeParam(modelId, id);
                InstanceNode instanceNode = new InstanceNode(nodeParam);
                list.add(instanceNode);
            }
            ModelAssetUtils.deleteInstanceNode(session, list);
        } catch (Exception e) {
            log.error("deleteRelationByInstances", e);
        }
    }

    @Override
    public List<Integer> queryInstanceIdExistTopo(Integer modelId, List<Integer> instanceIds) {
        List<Integer> list = new ArrayList();
        try {
            Session session = connectionPool.getSession();
            Set<String> setList = ModelAssetUtils.queryInstanceIdExistTopo(session, modelId, instanceIds);
            for (String node : setList) {
                if (!Strings.isNullOrEmpty(node) && node.split("_").length>0) {
                    list.add(intValueConvert(node.split("_")[1]));
                }
            }
        } catch (Exception e) {
            log.error("queryInstanceIdExistTopo", e);
        }
        return list;
    }

    @Override
    public Reply insertInstanceView(AddMwInstanceViewParam param) {
        try {
            MwInstanceViewDTO view = new MwInstanceViewDTO();
            long id = moduleIDManager.getID(IDModelType.Model);
            view.setId(id);
            view.extractFromParam(param);
            view.setCreator(iLoginCacheInfo.getLoginName());
            view.setModifier(iLoginCacheInfo.getLoginName());
            mwInstanceViewDao.insert(view);
        } catch (Exception e) {
            log.error("insertInstanceView", e);
            return Reply.fail(ErrorConstant.COMMON_MSG_200005);
        }

        return Reply.ok();
    }

    @Override
    public Reply deleteInstanceViewById(long id) {
        try {
            mwInstanceViewDao.deleteById(id);

            Session session = connectionPool.getSession();
            String space = ModelAssetUtils.INSTANCE_VIEW_SPACE + id;
            ModelAssetUtils.deleteInstanceTopo(session, space);

        } catch (Exception e) {
            log.error("deleteInstanceViewById", e);
            return Reply.fail(ErrorConstant.COMMON_MSG_200007);
        }
        return Reply.ok();
    }

    @Override
    public Reply updateInstanceView(UpdMwInstanceViewParam param) {
        try {
            MwInstanceViewDTO view = new MwInstanceViewDTO();
            view.extractFromParam(param);
            view.setModifier(iLoginCacheInfo.getLoginName());
            mwInstanceViewDao.update(view);
        } catch (Exception e) {
            log.error("updateInstanceView", e);
            return Reply.fail(ErrorConstant.COMMON_MSG_200006);
        }

        return Reply.ok();
    }

    @Override
    public Reply findInstanceViewById(long id) {

        try {
            MwInstanceViewDTO mwInstanceViewDTO = mwInstanceViewDao.findById(id);
            return Reply.ok(mwInstanceViewDTO);
        } catch (Exception e) {
            log.error("findInstanceViewById", e);
        }
        return Reply.fail(id);
    }

    @Override
    public Reply findAllInstanceView(SelMwInstanceViewParam selMwInstanceViewParam) {
        try {
            MwInstanceViewDTO mwInstanceViewDTO = new MwInstanceViewDTO();
            mwInstanceViewDTO.setInstanceId(selMwInstanceViewParam.getInstanceId());
            List<MwInstanceViewDTO> list = mwInstanceViewDao.findAll(mwInstanceViewDTO);

            //增加默认视图
            MwInstanceViewDTO defaultView = MwInstanceViewDTO.getDefault();
            list.add(0, defaultView);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("findAllInstanceView", e);
        }
        return Reply.fail(ErrorConstant.COMMON_MSG_200008);
    }

}
