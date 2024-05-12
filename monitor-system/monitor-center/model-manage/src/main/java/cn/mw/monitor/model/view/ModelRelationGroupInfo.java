package cn.mw.monitor.model.view;

import cn.mw.monitor.graph.modelAsset.ModelRelationDTO;
import cn.mw.monitor.graph.modelAsset.ModelRelationInfo;
import cn.mw.monitor.model.dto.RelationGroupDTO;
import cn.mw.monitor.model.service.MwModelRelationsService;
import cn.mw.monitor.util.ListMapObjUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class ModelRelationGroupInfo {
    private String id;
    private String label;
    private Integer realGroupId;
    private List<ModelRelationOppoInfo> modelList = new ArrayList<>();

    public void extractFrom(RelationGroupDTO data , List<ModelRelationDTO> relationDTOS , int ownModelId){
        this.id = data.getId();
        this.label = data.getLabel();
        this.realGroupId = data.getRealGroupId();

        if(null != relationDTOS){
            for(ModelRelationDTO modelRelationDTO : relationDTOS){
                ModelRelationOppoInfo modelRelationOppoInfo = new ModelRelationOppoInfo();
                Map map = modelRelationDTO.getRelationInfoMap();
                for(Object mapData : map.values()){
                    try {
                        ModelRelationInfo modelRelationInfo = ListMapObjUtils.mapToBean((Map) mapData, ModelRelationInfo.class);
                        if (modelRelationInfo.getModelId() != ownModelId) {
                            modelRelationOppoInfo.setValue(modelRelationInfo.getModelId());
                            modelRelationOppoInfo.setNum(Integer.parseInt(modelRelationInfo.getNum()));
                        } else {
                            modelRelationOppoInfo.setName(modelRelationInfo.getRelationName());
                        }
                    }catch (Exception e){
                        log.warn("extractFrom {}" ,e.toString());
                    }
                }
                modelList.add(modelRelationOppoInfo);
            }
        }
    }
}
