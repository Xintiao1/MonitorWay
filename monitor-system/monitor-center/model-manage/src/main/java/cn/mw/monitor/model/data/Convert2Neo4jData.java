package cn.mw.monitor.model.data;

import cn.mw.monitor.service.graph.EdgeParam;
import cn.mw.monitor.service.model.dto.InstanceLine;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.graph.NodeParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Convert2Neo4jData {
    private NodeParam rootNodeParam = null;
    private List<InstanceNode> nodes;
    private List<InstanceLine> lines;

    public void convertFrom(List<NodeParam> nodeParams ,List<EdgeParam> edgeParams){
        Map<String, NodeParam> nodeMap = new HashMap<>();
        Map<String, InstanceNode> integerInstanceMap = new HashMap<>();

        lines = new ArrayList<>();
        nodes = new ArrayList<>();

        //只有节点间存在连接关系,才会保存到neo4j中
        if (null != nodeParams && nodeParams.size() > 0) {
            for (NodeParam nodeParam : nodeParams) {
                if (nodeParam.getLevel().equals(0)) {
                    rootNodeParam = nodeParam;
                }
                nodeMap.put(nodeParam.getId(), nodeParam);
            }
        }

        if(null != edgeParams && edgeParams.size() > 0) {
            for (EdgeParam edgeParam : edgeParams) {
                InstanceNode startInstance = null;
                String startKey = edgeParam.getSource();
                NodeParam startNodeParam = nodeMap.get(startKey);
                if (null != startNodeParam) {
                    startInstance = integerInstanceMap.get(startKey);
                    if (null == startInstance) {
                        startInstance = new InstanceNode(startNodeParam);
                        integerInstanceMap.put(startKey, startInstance);
                    }
                }

                InstanceNode endInstance = null;
                String endKey = edgeParam.getTarget();
                NodeParam endNodeParam = nodeMap.get(endKey);
                if (null != endNodeParam) {
                    endInstance = integerInstanceMap.get(endKey);
                    if (null == endInstance) {
                        endInstance = new InstanceNode(endNodeParam);
                        integerInstanceMap.put(endKey, endInstance);
                    }
                }

                if (null != startInstance && null != endInstance) {
                    InstanceLine instanceLine = new InstanceLine(startInstance, endInstance);
                    lines.add(instanceLine);
                }
            }
            nodes.addAll(integerInstanceMap.values());
        }
    }

    public NodeParam getRootNodeParam() {
        return rootNodeParam;
    }

    public List<InstanceNode> getNodes() {
        return nodes;
    }

    public List<InstanceLine> getLines() {
        return lines;
    }
}
