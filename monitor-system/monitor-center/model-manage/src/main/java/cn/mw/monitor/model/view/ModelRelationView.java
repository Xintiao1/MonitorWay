package cn.mw.monitor.model.view;

import cn.mw.monitor.graph.modelAsset.ModelGroupParam;
import cn.mw.monitor.graph.modelAsset.ModelRelate;
import cn.mw.monitor.graph.modelAsset.ModelRelationDTO;
import cn.mw.monitor.model.dto.RelationGroupDTO;
import cn.mw.monitor.model.service.MwModelRelationsService;
import cn.mw.monitor.model.util.ModelUtils;
import cn.mw.monitor.service.model.dto.ModelInfo;
import lombok.Data;
import java.util.*;

@Data
public class ModelRelationView {

    private List nodes = new ArrayList();
    private List<ModelRelationEdge> edges = new ArrayList<>();

    public void extractFrom(Iterator iterator , List<RelationGroupDTO> groupList , ModelInfo modelInfo){
        //设置模型信息
        String showId = MwModelRelationsService.MODEL_PREFIX + modelInfo.getModelId();
        ModelRelationNode modelRelationNode = new ModelRelationNode();
        modelRelationNode.setId(showId);
        modelRelationNode.setRealModelId(modelInfo.getModelId());
        modelRelationNode.setImg(modelInfo.getModelIcon());
        modelRelationNode.setLabel(modelInfo.getModelName());
        nodes.add(modelRelationNode);

        //关系信息按分组归类
        Map<Integer ,List<ModelRelationDTO>> groupMap = new HashMap<>();
        while (iterator.hasNext()){
            //遍历关系
            Map<String ,Object> data = (Map)iterator.next();
            Object obj = data.get(ModelUtils.RELATION_KEY);
            ModelRelationDTO modelRelationDTO = null;
            if(obj instanceof ModelRelate){
                ModelRelate modelRelate = (ModelRelate) obj;
                modelRelationDTO = modelRelate.getModelRelationDTO();
            }

            if(null != modelRelationDTO){
                for(RelationGroupDTO groupData : groupList){
                    Integer groupId = groupData.getRealGroupId();
                    ModelGroupParam modelGroupParam = new ModelGroupParam(modelInfo.getModelId() ,groupId);

                    //判断关系是否属于该分组
                    if(modelRelationDTO.isBelongGroup(modelGroupParam)){
                        List<ModelRelationDTO> dataList = groupMap.get(modelGroupParam.getGroupId());
                        if(null == dataList){
                            dataList = new ArrayList<>();
                            groupMap.put(modelGroupParam.getGroupId() ,dataList);
                        }
                        dataList.add(modelRelationDTO);
                    }
                }
            }
        }

        for(RelationGroupDTO data : groupList){
            Integer groupId = data.getRealGroupId();
            Boolean defautGroupFlag = data.isDefautGroupFlag();
            List<ModelRelationDTO> relationDTOS = groupMap.get(groupId.intValue());

            //用户新建分组时,此时没有关系,需要显示分组
            if(null != relationDTOS){
                doAddGroupInfo(data ,relationDTOS ,modelInfo.getModelId() ,showId);
            }else if(!defautGroupFlag){
                doAddGroupInfo(data ,relationDTOS ,modelInfo.getModelId() ,showId);
            }
        }
    }

    private void doAddGroupInfo(RelationGroupDTO data , List<ModelRelationDTO> relationDTOS ,int ownModelId ,String showId){
        ModelRelationGroupInfo modelRelationGroupInfo = new ModelRelationGroupInfo();
        modelRelationGroupInfo.extractFrom(data ,relationDTOS ,ownModelId);
        nodes.add(modelRelationGroupInfo);

        ModelRelationEdge edge = new ModelRelationEdge();
        edge.setSource(showId);
        edge.setTarget(modelRelationGroupInfo.getId());
        edges.add(edge);
    }
}
