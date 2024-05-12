package cn.mw.monitor.graph.topo;

import cn.mw.monitor.graph.neo4j.action.DeleteAction;
import cn.mw.monitor.neo4j.ConnectionPool;
import cn.mw.monitor.service.topo.api.GraphDBCallback;
import cn.mw.monitor.service.topo.api.MwTopoGraphDBService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class MwTopoGraphDBServiceImpl implements MwTopoGraphDBService {
    private Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();
    private AtomicInteger taskNum = new AtomicInteger();

    @Autowired(required = false)
    private ConnectionPool connectionPool;

    @Override
    public void addTopoToGraphDB(String topoId ,String topoGraph , GraphDBCallback graphDBCallback) throws Exception{

        Session session = connectionPool.getSession();
        AddTopoTask addTopoTask = new AddTopoTask(topoId ,topoGraph ,session ,graphDBCallback);
        taskQueue.add(addTopoTask);
        executeTask();
    }

    @Override
    public String listTopoToGraphDB() throws Exception {
        Session session = connectionPool.getSession();
        Iterable<Neo4jNode> nodes = session.loadAll(Neo4jNode.class);
        Neo4jNode root = null;
        for(Neo4jNode node: nodes){
            if(node.isRoot()){
                root = node;
            }
        }

        String json = JSONObject.toJSONString(root);
        return json;
    }

    @Override
    public void removeTopoFromGraphDB(String topoId, GraphDBCallback graphDBCallback) {
        Session session = null;
        try {
            session = connectionPool.getSession();
            Node node = Cypher.node(topoId);
            node.named("n");
            Statement statement = Cypher.match(node).returning(node).build();
            Iterable<Neo4jNode> neo4jNodes = session.query(Neo4jNode.class, statement.getCypher(), Collections.emptyMap());

            for (Neo4jNode neo4jNode : neo4jNodes) {
                if (neo4jNode.getBindTopoIds().size() == 1
                        && neo4jNode.getBindTopoIds().contains(topoId)) {
                    neo4jNode.setPendingDel(true);
                    session.save(neo4jNode);
                }
            }

            DeleteAction deleteAction = new DeleteAction();
            deleteAction.setLabel(topoId);
            deleteAction.setRelation("TopoConnect");
            deleteAction.setNodeCondition("pendingDel");
            log.info(deleteAction.getCommand());
            session.query(Neo4jNode.class, deleteAction.getCommand(), Collections.emptyMap());

            if (null != graphDBCallback) {
                graphDBCallback.callback();
            }
        }catch (Exception e){
            log.error("removeTopoFromGraphDB" ,e);
        }
    }


    private void executeTask(){
        int num = taskNum.getAndIncrement();
        if(num == 0){
            log.info("task num is {} start thread" ,num);
            //启动执行队列线程
            ExcecuteThread thread = new ExcecuteThread(taskQueue ,taskNum);
            thread.start();
        }else{
            log.info("task num is {} increase num" ,num);
        }
    }
}
