package cn.mw.monitor.model.dto;

import java.util.ArrayList;
import java.util.List;

public class ModelInstanceTopoInfoV2 {
    //隐藏模型拓扑模型ids
    private List<Long> hideModelIds;

    //显示模型拓扑模型ids
    private List<Long> showModelIds;

    public void addAllHideModelIds(List<Long> ids){
        if(null == hideModelIds){
            hideModelIds = new ArrayList<>();
        }
        addAll(hideModelIds ,showModelIds ,ids);
    }

    public void addAllShowModelIds(List<Long> ids){
        if(null == showModelIds){
            showModelIds = new ArrayList<>();
        }
        addAll(showModelIds ,hideModelIds ,ids);
    }

    private void addAll(List<Long> addSet ,List<Long> removeSet ,List<Long> ids){
        for(Long id : ids){
            if(!addSet.contains(id)){
                addSet.add(id);
            }
        }

        if(null != removeSet){
            removeSet.removeAll(ids);
        }
    }

    public List<Long> getHideModelIds() {
        return hideModelIds;
    }

    public void setHideModelIds(List<Long> hideModelIds) {
        this.hideModelIds = hideModelIds;
    }

    public List<Long> getShowModelIds() {
        return showModelIds;
    }

    public void setShowModelIds(List<Long> showModelIds) {
        this.showModelIds = showModelIds;
    }
}
