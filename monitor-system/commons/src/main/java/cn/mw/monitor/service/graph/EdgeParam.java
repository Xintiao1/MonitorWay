package cn.mw.monitor.service.graph;

import lombok.Data;

import java.util.*;

@Data
public class EdgeParam {
    public static final String SEP = "_";
    private String source;
    private String target;
    private String key;

    private Integer sourceModelId;
    private Integer sourceInstanceId;

    private Integer targetModelId;
    private Integer targetInstanceId;


    public EdgeParam(){
    }

    public EdgeParam(String source ,String target){
        this.source = source;
        this.target = target;
        setKey(source ,target);
    }

    public EdgeParam(NodeParam src , NodeParam dest){
        this.source = src.getId();
        this.target = dest.getId();
        setKey(src.getId() ,dest.getId());
    }

    public void setKey(String srcId ,String destId){
        List<String> list = new ArrayList<>();
        list.add(srcId);
        list.add(destId);
        Collections.sort(list);
        this.key = list.toString();

        String[] srcValues = srcId.split(SEP);
        this.sourceModelId = Integer.parseInt(srcValues[0]);
        this.sourceInstanceId = Integer.parseInt(srcValues[1]);

        String[] destValues = destId.split(SEP);
        this.targetModelId = Integer.parseInt(destValues[0]);
        this.targetInstanceId = Integer.parseInt(destValues[1]);
    }

    public List<Integer> findTargetIntFormat(){
        List<Integer> list = new ArrayList<>();
        list.add(targetModelId);
        list.add(targetInstanceId);
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeParam edgeParam = (EdgeParam) o;
        return Objects.equals(source, edgeParam.source) &&
                Objects.equals(target, edgeParam.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }
}
