package cn.mw.monitor.path;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PathManage {
    private boolean debug;
    private PathManageCallback pathManageCallback = new DefaultPathCallback();

    public PathManageCallback getPathManageCallback() {
        return pathManageCallback;
    }

    public void setPathManageCallback(PathManageCallback pathManageCallback) {
        this.pathManageCallback = pathManageCallback;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void pathAnalyse(){
        List<GNode> rootList = pathManageCallback.genRootNodes();
        if(isDebug()){
            String result = rootList.stream().map(GNode::getName).collect(Collectors.joining(","));
            log.info("pathAnalyse genRootNodes [{}]" ,result);
        }

        if(null != rootList) {
            List<List<GNode>> all = new ArrayList<>();
            for (GNode root : rootList) {
                List<GNode> pathList = new ArrayList<>();
                pathList.add(root);
                List<List<GNode>> ret = findPath(root, pathList);
                if (isDebug()) {
                    log(ret);
                }
                all.addAll(ret);
            }

            findAndDelPathByScore(all);
        }
    }

    private void findAndDelPathByScore(List<List<GNode>> all){
        while (true) {
            //从所有的路径中提取节点
            List<PathNode> pathNodeList = PathNodeUtils.genPathNodeList(all);
            PathNode firstPathNode = PathNodeUtils.getFirstMultiPath(pathNodeList);

            //选择访问路径,获取需要删除的路径
            if (null != firstPathNode) {
                if(debug){
                    log.info("firstPathNode {}" ,firstPathNode.toString());
                }

                List<PrevPath> prevPaths = new ArrayList<>(firstPathNode.getPrevPathMap().values());
                List<PrevPath> pendingDelPrevPaths = pathManageCallback.choosePendingDelPath(prevPaths, firstPathNode);
                for (PrevPath prevPath : pendingDelPrevPaths) {
                    List<List<GNode>> paths = prevPath.getReleatedPath();
                    all.removeAll(paths);

                    //从删除的路径中继续分析
                    findAndDelPathByScore(paths);
                }
            } else {
                break;
            }
        }
    }

    private void log(List<List<GNode>> all){
        log.info("path size:{}" ,all.size());
        for(List<GNode> list : all){
            String result = list.stream().map(GNode::toString).collect(Collectors.joining(","));
            log.info(result);
        }
        log.info("----------------------------");
    }

    private List<List<GNode>> findPath(GNode tNode ,List<GNode> list) {
        List<List<GNode>> all = new ArrayList<>();
        for (GNode child : tNode.getChilds()) {
            List<GNode> childs = new ArrayList<>();
            childs.addAll(list);
            childs.add(child);
            List<List<GNode>> ret = findPath(child, childs);
            if(ret.size() > 0){
                all.addAll(ret);
            }else{
                all.add(childs);
            }

        }
        return all;
    }
}
