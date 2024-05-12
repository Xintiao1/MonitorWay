package cn.mw.monitor.model.view;
import cn.mw.monitor.graph.modelAsset.LastData;
import cn.mw.monitor.model.dto.ModelInstanceTopoInfo;
import cn.mw.monitor.service.graph.EdgeParam;
import cn.mw.monitor.service.graph.NodeParam;
import lombok.Data;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class InstanceTopoView {
    private LastData data;
    private LastData lastData;
    private HideModelDataView hideModelData;
    private Long intanceViewId;

    public void extractFrom(ModelInstanceTopoInfo modelInstanceTopoInfo){
        if(null != modelInstanceTopoInfo){
            lastData = modelInstanceTopoInfo.getLastData();
            data = modelInstanceTopoInfo.getData();
            HideModelDataView hideModelDataView = new HideModelDataView();
            hideModelDataView.setHide(modelInstanceTopoInfo.getHideModelIds());
            hideModelDataView.setShow(modelInstanceTopoInfo.getShowModelIds());
        }
    }

    //添加边,并且根据隐藏显示节点,设置拓扑显示
    public void addEdgesAndRefreshData(List<EdgeParam> edgeParams){
        addEdges(edgeParams);
        List<EdgeParam> edgeParams1 = new ArrayList<>();
        for(EdgeParam edge : this.lastData.getEdges()){
            if(null != hideModelData.getHide() &&
            (hideModelData.getHide().contains(edge.getSource()) || hideModelData.getHide().contains(edge.getTarget()))){
                continue;
            }
            edgeParams1.add(edge);
        }
        this.data.setEdges(edgeParams1);
    }

    public void addEdges(List<EdgeParam> edgeParams){
        //构造起始点为key的map
        if(null != edgeParams){
            Map<String ,List<EdgeParam>> startMap = edgeParams.stream().collect(Collectors.groupingBy(EdgeParam::getSource));
            Map<String ,Integer> nodePosMap = new HashMap<>();

            //设置节点排列位置
            for(int i=0;i<lastData.getNodes().size();i++){
                NodeParam nodeParam = lastData.getNodes().get(i);
                nodePosMap.put(nodeParam.getId() ,i);
            }

            List<EdgeParam> newEdges = new ArrayList<>();
            for(NodeParam nodeParam : lastData.getNodes()){
                //取出起始点关联的边
                List<EdgeParam> edgeParams1 = startMap.get(nodeParam.getId());

                //按照lastdata中节点顺序排序
                if(null != edgeParams1){
                    List<SortEdge> sortEdges = new ArrayList<>();
                    for(EdgeParam edge : edgeParams1){
                        Integer pos = nodePosMap.get(edge.getTarget());
                        SortEdge sortEdge = new SortEdge(edge ,pos);
                        sortEdges.add(sortEdge);
                    }
                    Collections.sort(sortEdges);

                    for(SortEdge sortEdge : sortEdges){
                        newEdges.add(sortEdge.getEdgeParam());
                    }
                }
            }

            lastData.setEdges(newEdges);
        }
    }

    public void initHideModelDataView(){
        this.hideModelData = new HideModelDataView();
        this.hideModelData.setHide(new ArrayList<>());
        this.hideModelData.setShow(new ArrayList<>());
    }
}
