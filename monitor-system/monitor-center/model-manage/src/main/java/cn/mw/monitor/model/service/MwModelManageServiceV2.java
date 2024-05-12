package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.model.param.AddAndUpdateModelPropertiesParam;
import cn.mwpaas.common.model.Reply;

import java.io.IOException;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/5 15:09
 */
public interface MwModelManageServiceV2 {
    Reply creatModel(AddAndUpdateModelParamV2 addAndUpdateModelParam) throws IOException;

    Reply updateModel(AddAndUpdateModelParamV2 addAndUpdateModelParam);

    Reply deleteModel(AddAndUpdateModelParamV2 modelParam, Boolean isDelete) throws Exception;

    Reply queryParentModelInfo();

    Reply queryOrdinaryModelInfo(AddAndUpdateModelGroupParamV2 groupParam);

    Reply selectOrdinaryModel(RelationModelDataParamV2 param);

    Reply selectModelList(ModelParamV2 modelParam);

    Reply deleteModelRelations(DeleteModelRelationParam param);

    Reply selectModelRelationsByModelId(SelectRelationParam param);

    Reply selectModelRelationsGroup(SelectRelationParam param);

    Reply editorPropertiesSort(List<ModelPropertiesSortParam> param);

    Reply creatModelProperties(AddAndUpdateModelPropertiesParam propertiesParam);

    Reply updateModelProperties(AddAndUpdateModelPropertiesParam addAndUpdateModelParam);

    Reply updateAllModelProperties();

    Reply updateModelPropertiesByGroup(AddAndUpdateModelGroupParam param);

    Reply deleteModelProperties(List<AddAndUpdateModelPropertiesParam> propertiesParamListm) throws IOException;

    Reply updateModelPropertiesShowStatus(AddAndUpdateModelPropertiesParam propertiesParam);

    Reply getPropertiesByManage(Integer modelId);

    Reply getPropertiesByBaseShow(Integer showType, Integer modelId);

    Reply selectModelInstanceFiledBySecond(Integer modelId);

    Reply getPropertiesInfoByModelId(ModelParam modelParam);

    Reply selectModelPropertiesList(ModelParam modelParam);

    Reply selectModelTypeListTree(QueryModelTypeParam param);

    Reply creatModelGroup(AddAndUpdateModelGroupParam groupParam);

    Reply updateModelGroup(AddAndUpdateModelGroupParam groupParam);

    Reply queryModelGroupById(AddAndUpdateModelGroupParam groupParam);

    Reply getAllModelGroupInfo();

    Reply deleteModelGroup(DeleteModelGroupParam deleteModelGroupParam);

    Reply queryModelListInfo();

    Reply selectModelListByModelId(ModelParam modelParam) throws Exception;

    Reply selectFatherModelList();

    Reply editorModelPropertiesType(AddAndUpdateModelPropertiesParam param);

    Reply queryPropertiesTypeList(AddAndUpdateModelPropertiesParam param);

    Reply checkModelByES();

    Reply syncModelToES();

    Reply cleanAllModelInfo();

    Reply selectGroupServerMap(Integer assetsSubTypeId);

    Reply queryPropertiesGanged(QueryModelGangedFieldParam param);

    Reply getPropertiesFieldByGanged(AddAndUpdateModelPropertiesParam param);
}
