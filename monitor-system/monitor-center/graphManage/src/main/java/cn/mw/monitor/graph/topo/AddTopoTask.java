package cn.mw.monitor.graph.topo;

import cn.mw.monitor.service.topo.api.GraphDBCallback;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.session.Session;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

@Slf4j
public class AddTopoTask implements Runnable{
    private String topoId;
    private String topoGraph;
    private Session session;
    private GraphDBCallback graphDBCallback;

    public AddTopoTask(String topoId ,String topoGraph , Session session , GraphDBCallback graphDBCallback){
        this.topoId = topoId;
        this.topoGraph = topoGraph;
        this.session = session;
        this.graphDBCallback = graphDBCallback;
    }

    @Override
    public void run() {
        try{
            JAXBContext context = JAXBContext.newInstance(Graph.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader sr = new StringReader(topoGraph);
            Graph graph = (Graph) unmarshaller.unmarshal(sr);
            Group group = graph.getGroup().get(0);
            Map<Integer, List<Line>> lineMap = group.getLineMapByStartIndex();
            Map<Integer, Node> nodeMap = group.getNodeIndexMap();

            Node root = null;
            for(Node node: nodeMap.values()){
                if(node.isRoot()){
                    root = node;
                    break;
                }
            }

            Neo4jNode node = visitGraph(topoId ,root ,null ,lineMap ,nodeMap);
            session.save(node);
            if(null != graphDBCallback){
                graphDBCallback.callback();
            }
        }catch (Exception e){
            log.error("AddTopoTask" ,e);
        }
    }

    private Neo4jNode visitGraph(String topoId ,Node node ,Neo4jNode parent
            ,Map<Integer, List<Line>> lineMap ,Map<Integer ,Node> nodeMap){

        Neo4jNode neo4jNode = new Neo4jNode();
        neo4jNode.bindTopoId(topoId);
        neo4jNode.extractFromNode(node);

        if(null != parent){
            parent.addNeo4jNode(neo4jNode);
        }
        parent = neo4jNode;

        List<Line> lines = lineMap.get(node.getIndex());
        if(null != lines && lines.size() > 0){
            for(Line line : lines){
                Node child = nodeMap.get(line.getEndIndex());
                if(null != child){
                    visitGraph(topoId ,child ,parent ,lineMap ,nodeMap);
                }else{
                    log.info("visitGraph child is null {} " ,node.toString());
                    log.info("visitGraph {}" ,line.toString());
                }

            }
        }

        return parent;
    }
}
