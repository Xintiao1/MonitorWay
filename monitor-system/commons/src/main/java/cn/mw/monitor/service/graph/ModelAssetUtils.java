package cn.mw.monitor.service.graph;

import cn.mw.monitor.service.model.dto.InstanceLine;
import cn.mw.monitor.service.model.dto.InstanceNode;
import cn.mw.monitor.service.model.dto.VirtualGroup;
import cn.mw.monitor.service.model.param.MwModelToPoRelationInstanceParam;
import cn.mw.monitor.service.model.param.QueryToPoRelationInstanceInfo;
import cn.mwpaas.common.utils.CollectionUtils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

import java.util.*;

import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

@Slf4j
public class ModelAssetUtils {
    private static int DEFAULT_LEVEL = 0;
    public static String DEFAULT_INSTANCE_LABEL = "InstanceLabel";
    public static String MODEL_SPACE = "ModelAsset";
    public static String MODEL_RELATIONSHIP = "RelateModel";
    public static final String INSTANCE_PRE = "Instance";
    public static final String VIRTUAL_SPACE = "Virtual";
    public static final String CITRIX_SPACE = "Citrix";
    public static final String COMMON_SPACE = "mwModelId";
    public static final String INSTANCE_VIEW_SPACE = "InstanceView";

    private static final String BatcheMergeLine = "unwind $lines AS line match (a:#space {id:line.start.id}), (b:#space {id:line.end.id}) with a,b merge (a)-[:#space]->(b)";
    private static final String BatchSetNodeLabel = "unwind $nodes as node merge (n:#default_space:#space {id:node.id})";
    private static final String FindLevel = "MATCH p=((a:#space {id:$id})-[:#relationship*]-()) RETURN max(length(p)) as level;";
    private static final String FindTreeEdge = "MATCH (a:#space {id:'#id'}) CALL apoc.path.spanningTree(a,{labelFilter:'#space' ,minLevel:1 ,maxLevel:#level})  YIELD path RETURN nodes(path) as p";
    private static final String DeleteInstanceNode_Merge = "MERGE (:#space {id:'#id'})";
    private static final String DeleteInstanceNode_Connect = "MATCH (a:#defaultspace {id:'#startId'}) ,(b:#defaultspace {id:'#endId'}) WITH a,b MERGE (a)-[:#space]->(b);";
    private static final String DeleteInstanceNode_Label = "MATCH (n:#default_space {id:'#id'}) set n#spaces;";
    private static final String DeleteInstanceNode = "MATCH (n:#defaultspace) WHERE n.id IN [#ids] detach delete n;";

    public static int findModelRelateLevel(Session session, Integer modelId) {
        String query = FindLevel.replaceAll("#space", MODEL_SPACE)
                .replaceAll("#id", modelId.toString()).replaceAll("#relationship", MODEL_RELATIONSHIP);
        Map criteria = new HashMap();
        criteria.put("id", modelId);
        int ret = doLevelQuery(session, query, criteria);
        return ret;
    }

    public static int findTreeLevel(Session session, InstanceNode node, String space) {
        String query = FindLevel.replaceAll("#space", space)
                .replaceAll("#id", node.getId()).replaceAll("#relationship", space);
        Map criteria = new HashMap();
        criteria.put("id", node.getId());
        int ret = doLevelQuery(session, query, criteria);
        return ret;
    }

    private static int doLevelQuery(Session session, String query, Map criteria) {
        int ret = 0;
        Result result = session.query(query, criteria);
        Iterator iterator = result.iterator();
        while (iterator.hasNext()) {
            Map<String, Long> map = (Map) iterator.next();
            Long retLong = map.get("level");
            if (null == retLong) {
                ret = DEFAULT_LEVEL;
            } else {
                ret = map.get("level").intValue();
            }

        }
        return ret;
    }

    public static void addInstanceTopo(Session session, String space
            , List<InstanceNode> instanceNodeList, List<InstanceLine> instanceLineList) {

        Transaction tx = session.beginTransaction();
        try {
            //创建节点,并设置标签
            if (null != instanceNodeList && instanceNodeList.size() > 0) {
                String resetLabel = BatchSetNodeLabel.replaceAll("#default_space", DEFAULT_INSTANCE_LABEL)
                        .replaceAll("#space", space);
                Map nodeMap = new HashMap();
                nodeMap.put("nodes", instanceNodeList);
                session.query(resetLabel, nodeMap);
            }

            //增加关系
            if (null != instanceLineList && instanceLineList.size() > 0) {
                String query = BatcheMergeLine.replaceAll("#space", space);
                Map map = new HashMap();
                map.put("lines", instanceLineList);
                session.query(query, map);

                query = BatcheMergeLine.replaceAll("#space", DEFAULT_INSTANCE_LABEL);
                map = new HashMap();
                map.put("lines", instanceLineList);
                session.query(query, map);
            }
            tx.commit();
        } catch (Throwable th) {
            log.error("Error while inserting mock data", th);
            tx.rollback();
        } finally {
            tx.close();
        }
    }


    public static void batchAddInstanceTopo(Session session, String space, List<VirtualGroup> virtualGroups) {
        Transaction tx = session.beginTransaction();
        try {
            for (VirtualGroup virtualGroup : virtualGroups) {
                List<InstanceNode> instanceNodeList = virtualGroup.getNodes();
                List<InstanceLine> instanceLineList = virtualGroup.getLineList();
                //创建节点,并设置标签
                if (null != instanceNodeList && instanceNodeList.size() > 0) {
                    String resetLabel = BatchSetNodeLabel.replaceAll("#default_space", DEFAULT_INSTANCE_LABEL)
                            .replaceAll("#space", space);
                    Map nodeMap = new HashMap();
                    nodeMap.put("nodes", instanceNodeList);
                    session.query(resetLabel, nodeMap);
                }

                //增加关系
                if (null != instanceLineList && instanceLineList.size() > 0) {
                    String query = BatcheMergeLine.replaceAll("#space", space);
                    Map map = new HashMap();
                    map.put("lines", instanceLineList);
                    session.query(query, map);

                    query = BatcheMergeLine.replaceAll("#space", DEFAULT_INSTANCE_LABEL);
                    map = new HashMap();
                    map.put("lines", instanceLineList);
                    session.query(query, map);
                }
            }
            tx.commit();
        } catch (Throwable th) {
            log.error("Error while inserting mock data", th);
            tx.rollback();
        } finally {
            tx.close();
        }
    }


    public static void deleteInstanceDirectTopo(Session session, String space, InstanceNode startNode) {
        deleteInstanceTopo(session, space, startNode);
    }

    public static void batchDeleteInstanceDirectTopo(Session session, String space, List<VirtualGroup> virtualGroups) {
        //删除关系
        Map<String, String> criteria = new HashMap<>();
        Transaction tx = session.beginTransaction();
        try {
            for (VirtualGroup virtualGroup : virtualGroups) {
                for (InstanceNode startNode : virtualGroup.getNodes()) {
                    String template = "match (#startnode)-[r:#space]-() delete r;";
                    String deleteRelation = null;
                    if (null == startNode) {
                        deleteRelation = template.replaceAll("#space", space).replaceAll("#startnode", "");
                    } else {
                        String startnodeStr = "n:" + space + " {" + "id:'" + startNode.getId() + "'}";
                        deleteRelation = template.replaceAll("#space", space).replaceAll("#startnode", startnodeStr);
                    }

                    session.query(deleteRelation, criteria);

                    //删除无连接关系的节点
                    template = "match (n:#space) where not(n)-[]-() with n delete n;";
                    String deleteNoRelationNode = template.replaceAll("#space", space);
                    session.query(deleteNoRelationNode, criteria);

                    //删除节点标签
                    template = "match(n:#space #id) remove n:#space;";
                    if (null == startNode) {
                        template = template.replaceAll("#id", "");
                    } else {
                        template = template.replaceAll("#id", startNode.genIdCondition());
                    }

                    String deleteTemplate = template.replaceAll("#space", space);
                    session.query(deleteTemplate, criteria);
                }
            }
            tx.commit();
        } catch (
                Throwable th) {
            log.error("deleteInstanceTopo", th);
            tx.rollback();
        } finally {
            tx.close();
        }

    }

    public static void deleteInstanceTopo(Session session, String space) {
        deleteInstanceTopo(session, space, null);
    }

    private static void deleteInstanceTopo(Session session, String space, InstanceNode startNode) {
        //删除关系
        Map<String, String> criteria = new HashMap<>();
        Transaction tx = session.beginTransaction();
        try {
            String template = "match (#startnode)-[r:#space]-() delete r;";

            String deleteRelation = null;
            if (null == startNode) {
                deleteRelation = template.replaceAll("#space", space).replaceAll("#startnode", "");
            } else {
                String startnodeStr = "n:" + space + " {" + "id:'" + startNode.getId() + "'}";
                deleteRelation = template.replaceAll("#space", space).replaceAll("#startnode", startnodeStr);
            }

            session.query(deleteRelation, criteria);

            //删除无连接关系的节点
            template = "match (n:#space) where not(n)-[]-() with n delete n;";
            String deleteNoRelationNode = template.replaceAll("#space", space);
            session.query(deleteNoRelationNode, criteria);

            //删除节点标签
            template = "match(n:#space #id) remove n:#space;";
            if (null == startNode) {
                template = template.replaceAll("#id", "");
            } else {
                template = template.replaceAll("#id", startNode.genIdCondition());
            }

            String deleteTemplate = template.replaceAll("#space", space);
            session.query(deleteTemplate, criteria);
            tx.commit();
        } catch (Throwable th) {
            log.error("deleteInstanceTopo", th);
            tx.rollback();
        } finally {
            tx.close();
        }
    }

    public static Set<String> queryInstanceIdExistTopo(Session session, Integer modelId, List<Integer> nodes) {
        Transaction tx = session.beginTransaction();
        Map<String, String> criteria = new HashMap<>();
        List<String> topoInstanceIdList = new ArrayList<>();
        try {
            StringBuffer sb = new StringBuffer();
            for (Integer node : nodes) {
                sb.append(modelId + "_" + node).append(",");
            }
            String str = sb.toString();
            String idsStr = "";
            if (!Strings.isNullOrEmpty(str)) {
                idsStr = sb.toString().substring(0, sb.toString().length() - 1);
            }
            String template = "MATCH (n) where n.id in [#ids] RETURN n.id AS id";
            String deleteNoRelationNode = template.replaceAll("#ids", idsStr);
            Result result = session.query(deleteNoRelationNode, criteria);
            Iterator iterator = result.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                List<String> idList = (List) map.get("id");
                topoInstanceIdList.addAll(idList);
            }
            tx.commit();
        } catch (Throwable th) {
            log.error("deleteNodeAndLine", th);
            tx.rollback();
        } finally {
            tx.close();
        }
        Set<String> set = new HashSet<>(topoInstanceIdList);
        return set;
    }

    public static List<MwModelToPoRelationInstanceParam> queryToPoInfoByInstanceList(Session session, List<QueryToPoRelationInstanceInfo> queryList) {
        //根据节点的id数据和目标节点的modelId 查询目标节点的具体信息（模糊查询）
        Transaction tx = session.beginTransaction();
        List<MwModelToPoRelationInstanceParam> topoInstanceIdList = new ArrayList<>();
        try {
            for (QueryToPoRelationInstanceInfo queryInfo : queryList) {
                String space = queryInfo.getSpace();
                String deep = queryInfo.getDeep();
                String ownModelId = queryInfo.getOwnModelId();
                String ownModelIndex = queryInfo.getOwnModelIndex();
                String ownInstanceId = queryInfo.getOwnInstanceId();
                String relationModelId = queryInfo.getRelationModelId();
                String ownModelInstanceId = ownModelId + "_" + ownInstanceId;
                Map<String, String> criteria = new HashMap<>();
                if (!ownModelId.equals(relationModelId)) {//起始节点和结束节点不为同一个模型下的实例
                    //有关系指向的查询n2 -> n1，标签为InstanceLabel的
                    //String template = "MATCH path = shortestPath((n2)-[:InstanceLabel*..5]->(n1)) WHERE n1.id = '504_454175' AND n2.id =~ '.*501_.*' RETURN n2";
                    //查询指定深度的和n1节点相关的n2节点信息（n2节点的id包含指定数据），返回最短路径中深度最小的节点数据

                    if (Strings.isNullOrEmpty(space)) {
                        space = "InstanceLabel";
                    }
                    String template = "MATCH path = shortestPath((n1)-[#space*..#deep]-(n2)) WHERE n1.id = '#ownModelInstanceId' AND n2.id =~ '.*#relationModelId_.*' RETURN n2 order by length(path) ASC limit 1";
                    String queryRelationNode = template.replaceAll("#space", space).replaceAll("#deep", deep)
                            .replaceAll("#ownModelInstanceId", ownModelInstanceId).replaceAll("#relationModelId", relationModelId);
                    log.info("queryRelationNode::" + queryRelationNode);
                    Result result = session.query(queryRelationNode, criteria);
                    Iterator iterator = result.queryResults().iterator();
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();
                        NodeModel nodeModel = (NodeModel) map.get("n2");
                        if (nodeModel != null && CollectionUtils.isNotEmpty(nodeModel.getPropertyList())) {
                            Object obj = nodeModel.getPropertyList().get(0).getValue();
                            if (obj != null && strValueConvert(obj).split("_").length > 1) {
                                MwModelToPoRelationInstanceParam relationInstanceParam = new MwModelToPoRelationInstanceParam();
                                String relationInstanceId = strValueConvert(obj).split("_")[1];
                                if(!"0".equals(relationInstanceId)){
                                    relationInstanceParam.setRelationInstanceId(intValueConvert(relationInstanceId));
                                    relationInstanceParam.setRelationModelId(intValueConvert(relationModelId));
                                    relationInstanceParam.setOwnInstanceId(intValueConvert(ownInstanceId));
                                    relationInstanceParam.setOwnModelId(intValueConvert(ownModelId));
                                    relationInstanceParam.setOwnModelIndex(ownModelIndex);
                                    topoInstanceIdList.add(relationInstanceParam);
                                }
                            }
                        }
                    }
                }
            }
            tx.commit();
        } catch (Throwable th) {
            log.error("queryTopoInfoByModelInstanceId", th);
            tx.rollback();
        } finally {
            tx.close();
        }
        return topoInstanceIdList;
    }


    public static List<String> queryTopoInfoByModelInstanceId(Session session, String space, String deep, String ownModelInstanceId, Set<String> relationModelIds) {
        //根据节点的id数据和目标节点的modelId 查询目标节点的具体信息（模糊查询）
        Transaction tx = session.beginTransaction();
        List<String> topoInstanceIdList = new ArrayList<>();
        try {
            for (String raletionModelId : relationModelIds) {
                Map<String, String> criteria = new HashMap<>();
                //有关系指向的查询n2 -> n1，标签为InstanceLabel的
//            String template = "MATCH path = shortestPath((n2)-[:InstanceLabel*..5]->(n1)) WHERE n1.id = '504_454175' AND n2.id =~ '.*501_.*' RETURN n2";
                //查询指定深度的和n1节点相关的n2节点信息（n2节点的id包含指定数据），返回最短路径中深度最小的节点数据
                if (Strings.isNullOrEmpty(space)) {
                    space = "InstanceLabel";
                }
                String template = "MATCH path = shortestPath((n1)-[#space*..#deep]-(n2)) WHERE n1.id = '#ownModelInstanceId' AND n2.id =~ '.*#raletionModelId_.*' RETURN n2 order by length(path) ASC limit 1";
                String queryRelationNode = template.replaceAll("#space", space).replaceAll("#deep", deep)
                        .replaceAll("#ownModelInstanceId", ownModelInstanceId).replaceAll("#raletionModelId", raletionModelId);
                Result result = session.query(queryRelationNode, criteria);
                Iterator iterator = result.queryResults().iterator();
                while (iterator.hasNext()) {
                    Map map = (Map) iterator.next();
                    NodeModel nodeModel = (NodeModel) map.get("n2");
                    if (nodeModel != null && CollectionUtils.isNotEmpty(nodeModel.getPropertyList())) {
                        Object obj = nodeModel.getPropertyList().get(0).getValue();
                        if (obj != null) {
                            topoInstanceIdList.add(obj.toString());
                        }
                    }
                }
            }
            tx.commit();
        } catch (Throwable th) {
            log.error("queryTopoInfoByModelInstanceId", th);
            tx.rollback();
        } finally {
            tx.close();
        }
        return topoInstanceIdList;
    }

    public static void deleteInstanceTopoBySpaceList(Session session, List<String> spaceList) {
        //删除关系
        Transaction tx = session.beginTransaction();
        try {
            for (String space : spaceList) {
                Map<String, String> criteria = new HashMap<>();
                String template = "match (n:#space) detach delete n";
                String deleteNoRelationNode = template.replaceAll("#space", space);
                session.query(deleteNoRelationNode, criteria);
            }
            tx.commit();
        } catch (Throwable th) {
            log.error("deleteInstanceTopoBySpaceList", th);
            tx.rollback();
        } finally {
            tx.close();
        }
    }

    //删除已某个点开始的所有关联space节点
    public static void deleteNodeAndLine(Session session, String space, InstanceNode startNode) {
        //删除关系
        Map<String, String> criteria = new HashMap<>();
        Transaction tx = session.beginTransaction();
        try {
            String template = "match (a:#space #id)-[:#space*]-(b) detach delete a,b;";
            String deleteNoRelationNode = template.replaceAll("#space", space)
                    .replaceAll("#id", startNode.genIdCondition());
            session.query(deleteNoRelationNode, criteria);
            tx.commit();
        } catch (Throwable th) {
            log.error("deleteNodeAndLine", th);
            tx.rollback();
        } finally {
            tx.close();
        }
    }

    public static void deleteInstanceNode(Session session, InstanceNode instanceNode) {
        List<InstanceNode> list = new ArrayList<>();
        list.add(instanceNode);
        deleteInstanceNode(session, list);
    }

    public static void deleteInstanceNode(Session session, List<InstanceNode> instanceNodes) {
        Map<String, String> criteria = new HashMap<>();
        Transaction tx = session.beginTransaction();
        try {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < instanceNodes.size(); i++) {
                InstanceNode node = instanceNodes.get(i);
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(node.genIdQuoteString());
            }

            //查询节点连接关系及对端节点
            String deleteNodeIds = sb.toString();
            String template = "match (a:#space)-[r]-(b) where a.id in [#ids] return a as src,labels(a) as labels,type(r) as relation,b as dest order by src";
            String matchQuery = template.replaceAll("#space", ModelAssetUtils.DEFAULT_INSTANCE_LABEL)
                    .replaceAll("#ids", deleteNodeIds);

            Result result = session.query(matchQuery, criteria);

            //根据起始点分组
            Map<String, List<InstanceRelateNode>> groupMap = new HashMap<>();

            Iterator iterator = result.iterator();
            while (iterator.hasNext()) {
                Map map = (Map) iterator.next();
                NodeModel from = (NodeModel) map.get("src");
                String relNode = map.get("relation").toString();
                NodeModel to = (NodeModel) map.get("dest");
                String[] labels = (String[]) map.get("labels");
                InstanceRelateNode instanceRelateNode = new InstanceRelateNode(from, to, relNode, labels);

                List<InstanceRelateNode> list = groupMap.get(instanceRelateNode.getFromId());
                if (null == list) {
                    list = new ArrayList<>();
                    groupMap.put(instanceRelateNode.getFromId(), list);
                }
                list.add(instanceRelateNode);
            }

            //按照分组重置节点
            Map map = new HashMap();
            for (List<InstanceRelateNode> list : groupMap.values()) {
                //确定要删除的
                InstanceRelateNode pendingDelNode = list.get(0);
                NodeParam resetNodeParam = pendingDelNode.getResetNodeParamByFrom();
                String resetAction = DeleteInstanceNode_Merge.replaceAll("#space", ModelAssetUtils.DEFAULT_INSTANCE_LABEL)
                        .replaceAll("#id", resetNodeParam.getId());
                session.query(resetAction, map);

                //原来的关系连接到重置节点
                String[] labels = null;
                for (int i = 0; i < list.size(); i++) {
                    InstanceRelateNode instanceRelateNode = list.get(i);
                    if (i == 0) {
                        labels = instanceRelateNode.getLabels();
                    }
                    String connAction = DeleteInstanceNode_Connect.replaceAll("#defaultspace", ModelAssetUtils.DEFAULT_INSTANCE_LABEL)
                            .replaceAll("#startId", resetNodeParam.getId())
                            .replaceAll("#endId", instanceRelateNode.getToId())
                            .replaceAll("#space", instanceRelateNode.getRelationShip());
                    session.query(connAction, map);
                }

                if (null != labels) {
                    StringBuffer labelsStr = new StringBuffer();
                    for (String value : labels) {
                        labelsStr.append(":").append(value);
                    }
                    String labelAction = DeleteInstanceNode_Label.replaceAll("#default_space", ModelAssetUtils.DEFAULT_INSTANCE_LABEL)
                            .replaceAll("#id", resetNodeParam.getId())
                            .replaceAll("#spaces", labelsStr.toString());
                    session.query(labelAction, map);
                }

                String deleteNodeAction = DeleteInstanceNode.replaceAll("#defaultspace", ModelAssetUtils.DEFAULT_INSTANCE_LABEL)
                        .replaceAll("#ids", deleteNodeIds);
                session.query(deleteNodeAction, map);
            }

            tx.commit();
        } catch (Throwable th) {
            log.error("Error while inserting mock data", th);
            tx.rollback();
        } finally {
            tx.close();
        }
    }

    public static List<EdgeParam> findInstanceEdgeBySpace(Session session, NodeParam start, int maxLevel) {
        String space = INSTANCE_PRE + start.getRealId();
        return findEdgeBySpace(session, start, space, maxLevel);
    }

    public static List<EdgeParam> findEdgeBySpace(Session session, NodeParam start, String space, int maxLevel) {
        return doFindEdgeBySpace(session, start, space, maxLevel, "RELATIONSHIP_PATH");
    }

    public static List<EdgeParam> findTreeEdgeBySpace(Session session, NodeParam start, String space) {
        return doFindEdgeBySpace(session, start, space, -1, "NODE_GLOBAL");
    }

    private static List<EdgeParam> doFindEdgeBySpace(Session session, NodeParam start, String space, int maxLevel
            , String strategy) {

        List<EdgeParam> ret = new ArrayList<>();
        String pathKey = "p";
        String query = FindTreeEdge.replaceAll("#space", space)
                .replaceAll("#id", start.getId()).replaceAll("#level", String.valueOf(maxLevel))
                .replaceAll("#strategy", strategy);
        Map criteria = new HashMap();
        criteria.put("space", space);
        criteria.put("start", start.getId());
        Result result = session.query(query, criteria);
        Iterator iterator = result.queryResults().iterator();
        while (iterator.hasNext()) {
            Map map = (Map) iterator.next();
            List<NodeModel> nodeModels = (List) map.get(pathKey);
            for (int i = 0; i < nodeModels.size() - 1; i++) {
                String startId = nodeModels.get(i)
                        .getPropertyList().get(0).toString().replaceAll("id :", "").trim();

                String endId = nodeModels
                        .get(i + 1).getPropertyList().get(0).toString().replaceAll("id :", "").trim();
                EdgeParam edgeParam = new EdgeParam(startId, endId);
                if (!ret.contains(edgeParam)) {
                    ret.add(edgeParam);
                }
            }
        }

        return ret;
    }
}
