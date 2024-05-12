package cn.mw.monitor.graph.modelAsset;

import cn.mw.monitor.service.graph.EdgeParam;
import cn.mw.monitor.service.graph.NodeParam;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LastData {
    private List<NodeParam> nodes;
    private List<EdgeParam> edges;
    private List<ComboParam> combos;

    public void addNodeParam(NodeParam nodeParam){
        if(null == nodes){
            nodes = new ArrayList<>();
        }
        nodes.add(nodeParam);
    }

    public void addEdgeParam(EdgeParam edgeParam){
        if(null == edges){
            edges = new ArrayList<>();
        }
        edges.add(edgeParam);
    }

    public void addComboParam(ComboParam comboParam){
        if(null == combos){
            combos = new ArrayList<>();
        }
        combos.add(comboParam);
    }
}
