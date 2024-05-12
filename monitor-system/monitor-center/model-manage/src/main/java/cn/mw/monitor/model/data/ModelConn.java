package cn.mw.monitor.model.data;

import cn.mw.monitor.service.graph.EdgeParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ModelConn {
    private Integer startModelId;
    private Integer endModelId;
    private String key;

    public ModelConn(Integer startModelId ,Integer endModelId){
        this.startModelId = startModelId;
        this.endModelId = endModelId;
        initKey(this.startModelId ,this.endModelId);
    }

    public ModelConn(EdgeParam edgeParam){
        String[] sourceStr = edgeParam.getSource().split(EdgeParam.SEP);
        this.startModelId = Integer.parseInt(sourceStr[0]);

        String[] targetStr = edgeParam.getTarget().split(EdgeParam.SEP);
        this.endModelId = Integer.parseInt(targetStr[0]);
        initKey(this.startModelId ,this.endModelId);
    }

    private void initKey(Integer startModelId ,Integer endModelId){
        List<Integer> values = new ArrayList<>();
        values.add(startModelId);
        values.add(endModelId);
        Collections.sort(values);
        this.key = values.get(0).toString() + "-" + values.get(1).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelConn modelConn = (ModelConn) o;
        return Objects.equals(key, modelConn.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
