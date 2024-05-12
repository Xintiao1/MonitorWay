package cn.mw.monitor.graph.modelAsset;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ModelRelationDTO {
    private List<Integer> groupIds;
    private Map relationInfoMap;

    public void addModelRelationInfo(ModelRelationInfo modelRelationInfo){
        if(null == relationInfoMap){
            relationInfoMap = new HashMap<>();
        }
        relationInfoMap.put(modelRelationInfo.getModelId() ,modelRelationInfo);
    }

    public void addGroupId(Integer groupId){
        if(null == groupIds){
            groupIds = new ArrayList<>();
        }
        groupIds.add(groupId);
    }

    public boolean isBelongGroup(ModelGroupParam modelGroupParam){
        boolean isCheckModelId = false;
        boolean isCheckGroupId = false;
        if(null != relationInfoMap){
            for(Object data : relationInfoMap.values()){
                if(data instanceof JSON){
                    ModelRelationInfo modelRelationInfo = JSONObject.toJavaObject((JSON)data ,ModelRelationInfo.class);
                    if(modelGroupParam.getOwnModelId().equals(modelRelationInfo.getModelId())){
                        isCheckModelId = true;
                        break;
                    }
                }
            }

            for(Integer groupId : groupIds){
                if(modelGroupParam.getGroupId().equals(groupId)){
                    isCheckGroupId = true;
                    break;
                }
            }
        }

        if(isCheckModelId && isCheckGroupId){
            return true;
        }

        return false;
    }
}
