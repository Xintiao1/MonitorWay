package cn.mw.monitor.model.view;

import cn.mw.monitor.graph.modelAsset.ModelAsset;
import cn.mw.monitor.graph.modelAsset.ModelRelate;
import cn.mw.monitor.graph.modelAsset.ModelRelationDTO;
import cn.mw.monitor.graph.modelAsset.ModelRelationInfo;
import cn.mw.monitor.util.ListMapObjUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class ModelAssociatedView {
    private Integer groupId;
    private String groupName;
    private String relationName;
    private Integer ownRelationNum;
    private Integer oppositeModelId;
    private Integer oppositeRelationNum;

    public static List<ModelAssociatedView> genViewList(ModelAsset modelAsset ,Integer groupId){
        List<ModelAssociatedView> list = new ArrayList<>();
        if(null != modelAsset.getModelRelates()){
            for(ModelRelate modelRelate : modelAsset.getModelRelates()){
                boolean find = false;
                ModelRelationDTO modelRelationDTO = modelRelate.getModelRelationDTO();
                for(Integer rGroupId : modelRelationDTO.getGroupIds()){
                    if(rGroupId.equals(groupId)){
                        find = true;
                        break;
                    }
                }

                if(find){
                    ModelAssociatedView modelAssociatedView = new ModelAssociatedView();
                    Map map = modelRelationDTO.getRelationInfoMap();
                    for(Object mapData : map.values()){
                        try {
                            ModelRelationInfo modelRelationInfo = ListMapObjUtils.mapToBean((Map) mapData, ModelRelationInfo.class);
                            if (modelAsset.getId().equals(modelRelationInfo.getModelId())) {
                                modelAssociatedView.setOwnRelationNum(Integer.parseInt(modelRelationInfo.getNum()));
                            }else{
                                modelAssociatedView.setRelationName(modelRelationInfo.getRelationName());
                                modelAssociatedView.setOppositeModelId(modelRelationInfo.getModelId());
                                modelAssociatedView.setOppositeRelationNum(Integer.parseInt(modelRelationInfo.getNum()));
                            }
                        }catch (Exception e){
                            log.warn("extractFrom {}" ,e.toString());
                        }
                    }

                    //设置对端分组id
                    for(Integer rGroupId : modelRelationDTO.getGroupIds()){
                        if(!rGroupId.equals(groupId)){
                            modelAssociatedView.setGroupId(rGroupId);
                            break;
                        }
                    }
                    list.add(modelAssociatedView);
                }
            }
        }
        return list;
    }
}
