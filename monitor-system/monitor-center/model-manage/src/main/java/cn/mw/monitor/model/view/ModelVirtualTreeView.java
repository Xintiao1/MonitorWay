package cn.mw.monitor.model.view;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModelVirtualTreeView {
    private List<VirtualView> virtual;
    private List<VirtualView> datastore;

    public void addVirtualTree(VirtualView root){
        if(null == virtual){
            virtual = new ArrayList<>();
        }
        virtual.add(root);
    }

    public void addDatastoreTree(VirtualView root){
        if(null == datastore){
            datastore = new ArrayList<>();
        }
        if(null != root.getChildren() && root.getChildren().size() > 0){
            for(VirtualView dataCenter : root.getChildren()){
                datastore.add(dataCenter);
            }
        }

    }
}
