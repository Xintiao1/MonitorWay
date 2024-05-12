package cn.mw.monitor.model.view;

import cn.mw.monitor.graph.modelAsset.ModelAsset;
import cn.mw.monitor.graph.modelAsset.ModelRelate;
import cn.mw.monitor.graph.modelAsset.ModelRelationDTO;
import cn.mw.monitor.graph.modelAsset.ModelRelationInfo;
import cn.mw.monitor.util.ListMapObjUtils;
import lombok.Data;

import java.util.Map;

@Data
public class RelationNumInstanceTopoView {
    private Integer ownModelId;
    private Integer ownRelationNum;
    private Integer oppositeModelId;
    private Integer oppositeRelationNum;

    public void extractFrom(ModelAsset modelAsset) throws Exception{
        if(null !=modelAsset && null != modelAsset.getModelRelates()){
            for(ModelRelate modelRelate : modelAsset.getModelRelates()){
                ModelRelationDTO modelRelationDTO = modelRelate.getModelRelationDTO();
                ModelRelationInfo ownRelation = null;
                ModelRelationInfo oppoRelation = null;
                for(Object mapData : modelRelationDTO.getRelationInfoMap().values()){
                    ModelRelationInfo modelRelationInfo = ListMapObjUtils.mapToBean((Map) mapData, ModelRelationInfo.class);
                    if(ownModelId.equals(modelRelationInfo.getModelId())){
                        ownRelation = modelRelationInfo;
                    }

                    if(oppositeModelId.equals(modelRelationInfo.getModelId())){
                        oppoRelation = modelRelationInfo;
                    }
                }

                if(null != ownRelation && null != oppoRelation){
                    ownRelationNum = Integer.parseInt(ownRelation.getNum());
                    oppositeRelationNum = Integer.parseInt(oppoRelation.getNum());
                }
            }
        }
    }
}
