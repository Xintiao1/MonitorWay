package cn.mw.monitor.model.view;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModelRancherTreeView {
    private List<RancherView> rancher;

    public void addRancherTree(RancherView root){
        if(null == rancher){
            rancher = new ArrayList<>();
        }
        rancher.add(root);
    }

}
