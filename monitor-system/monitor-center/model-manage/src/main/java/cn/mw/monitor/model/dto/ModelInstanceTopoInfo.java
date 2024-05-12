package cn.mw.monitor.model.dto;

import cn.mw.monitor.graph.modelAsset.LastData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class ModelInstanceTopoInfo {
    private LastData lastData;

    private LastData data;

    //隐藏模型拓扑模型ids
    private List<Integer> hideModelIds;

    //显示模型拓扑模型ids
    private List<Integer> showModelIds;

    public void addAllHideModelIds(List<Integer> ids){
        if(null == hideModelIds){
            hideModelIds = new ArrayList<>();
        }
        addAll(hideModelIds ,showModelIds ,ids);
    }

    public void addAllShowModelIds(List<Integer> ids){
        if(null == showModelIds){
            showModelIds = new ArrayList<>();
        }
        addAll(showModelIds ,hideModelIds ,ids);
    }

    private void addAll(List<Integer> addSet ,List<Integer> removeSet ,List<Integer> ids){
        for(Integer id : ids){
            if(!addSet.contains(id)){
                addSet.add(id);
            }
        }

        if(null != removeSet){
            removeSet.removeAll(ids);
        }
    }

    public List<Integer> getHideModelIds() {
        return hideModelIds;
    }

    public void setHideModelIds(List<Integer> hideModelIds) {
        this.hideModelIds = hideModelIds;
    }

    public List<Integer> getShowModelIds() {
        return showModelIds;
    }

    public void setShowModelIds(List<Integer> showModelIds) {
        this.showModelIds = showModelIds;
    }

    public LastData getLastData() {
        return lastData;
    }

    public void setLastData(LastData lastData) {
        this.lastData = lastData;
    }

    public LastData getData() {
        return data;
    }

    public void setData(LastData data) {
        this.data = data;
    }
}
