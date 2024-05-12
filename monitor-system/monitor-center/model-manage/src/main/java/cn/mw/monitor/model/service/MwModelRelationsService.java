package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.*;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author qzg
 * @date 2022/2/21
 */
public interface MwModelRelationsService {
    static final String GROUP_PREFIX = "group_";
    static final String MODEL_PREFIX = "model";
    static final String GROUP_LABEL_KEY = "label";
    static final String GROUP_REAL_ID_KEY = "realGroupId";
    static final String DEFAULT_GROUP_FLAG = "defautGroupFlag";
    static final String GROUP_ID_KEY = "id";
    static final String UNGROUP_NAME = "未分组";
    static final String UNGROUP_DESC = "自动创建未分组";

    Reply creatModelRelations(AddAndUpdateModelRelationParam param);

    Reply selectAllModelByRelationsExludeOwn(Integer ownModelId);

    Reply showModelRelations(AddAndUpdateModelRelationParam param);

    Reply selectModelRelationsByModelId(AddAndUpdateModelRelationParam param);

    Reply editorModelRelationsByModelId(AddAndUpdateModelRelationParam param);

    Reply deleteModelRelations(AddAndUpdateModelRelationParam param);

    Reply creatModelRelationsGroup(AddAndUpdateModelRelationGroupParam param);

    Reply updateModelRelationsGroup(AddAndUpdateModelRelationGroupParam param);

    Reply modelRelationsGroupByUpdate(AddAndUpdateModelRelationGroupParam param);

    Reply deleteModelRelationsGroup(DeleteModelRelationGroupParam param);

    //检查模型是否有关联关系
    boolean hasRelation(Integer modelId) throws Exception;

    //检查模型是否有关联关系
    void deleteModelNode(Integer modelId) throws Exception;

    Reply selectGroupList(QueryGroupListParam param);

    Reply queryModelRelationGroupBySelect(ModelRelationGroupsParam param);

    Reply addInstanceToPo(QueryInstanceRelationToPoParam param);

    Reply viewInstanceToPo(QueryInstanceRelationToPoParam param);

    Reply queryRelationNumInstanceToPo(QueryInstanceRelationToPoParam param);

    Reply deleteInstanceToPo(QueryInstanceRelationToPoParam param);

    Reply hideModelToPo(QueryInstanceRelationToPoParam param);

    Reply getTOPOModel(QueryInstanceRelationToPoParam param);

    //删除执行模型下的实例id的实例关系拓扑数据
    void deleteRelationByInstances(Integer modelId, List<Integer> instanceIds);

    List<Integer> queryInstanceIdExistTopo(Integer modelId, List<Integer> instanceIds);

    //插入实例视图一条记录
    Reply insertInstanceView(AddMwInstanceViewParam param);

    //根据id删除实例视图记录
    Reply deleteInstanceViewById(long id);

    //更新实例视图记录
    Reply updateInstanceView(UpdMwInstanceViewParam param);

    //根据id查询实例视图记录
    Reply findInstanceViewById(long id);

    //查询实例视图所有记录
    Reply findAllInstanceView(SelMwInstanceViewParam selMwInstanceViewParam);

}
