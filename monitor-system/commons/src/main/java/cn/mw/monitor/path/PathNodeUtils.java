package cn.mw.monitor.path;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PathNodeUtils {
    public static PathNode genPathNode(GNode tNode ,List<List<GNode>> allPath){
        PathNode pathNode = new PathNode(tNode);
        for(List<GNode> path : allPath){
            for(int i = 0;i<path.size();i++){
                GNode node = path.get(i);
                if(tNode.equals(node)){
                    List<GNode> subList = path.subList(0 ,i);
                    String result = subList.stream().map(GNode::toString).collect(Collectors.joining(PrevPath.PATH_SEP));
                    PrevPath prevPath = pathNode.getPrevPath(result);

                    if(null == prevPath){
                        prevPath = new PrevPath(result);
                        pathNode.addPrevPath(prevPath);
                    }
                    prevPath.addReleatedPath(path);
                    break;
                }
            }
        }

        return pathNode;
    }

    public static PathNode getFirstMultiPath(List<PathNode> pathNodes){
        for(PathNode pathNode : pathNodes){
            if(pathNode.getPrevPathNum() > 1){
                return pathNode;
            }
        }
        return null;
    }

    public static List<PathNode> genPathNodeList(List<List<GNode>> allPath){
        List<PathNode> pathNodes = new ArrayList<>();
        for(List<GNode> paths : allPath){
            for(GNode tNode : paths){
                PathNode checkNode = new PathNode(tNode);
                if(!pathNodes.contains(checkNode)){
                    PathNode pathNode = genPathNode(tNode ,allPath);
                    pathNodes.add(pathNode);
                }
            }
        }
        return pathNodes;
    }

    public static List<PrevPath> choosePendingDelPath(List<PrevPath> prevPaths){
        int maxLength = -1;
        List<PrevPath> ret = new ArrayList<>(prevPaths);
        PrevPath choosePath = null;
        for(PrevPath prevPath : prevPaths){
            int length = getMaxLength(prevPath);
            if(length > maxLength){
                maxLength = length;
                choosePath = prevPath;
            }
        }
        ret.remove(choosePath);
        return ret;
    }

    private static int getMaxLength(PrevPath prevPath){
        int maxLength = -1;
        for(List<GNode> path : prevPath.getReleatedPath()){
            if(path.size() > maxLength){
                maxLength = path.size();
            }
        }

        return maxLength;
    }
}
