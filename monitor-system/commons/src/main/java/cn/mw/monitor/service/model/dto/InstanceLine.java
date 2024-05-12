package cn.mw.monitor.service.model.dto;

import cn.mw.monitor.service.graph.ModelAssetUtils;
import com.alibaba.fastjson.annotation.JSONField;

public class InstanceLine {
    @JSONField(ordinal = 1)
    private InstanceNode start;
    @JSONField(ordinal = 2)
    private InstanceNode end;

    public InstanceLine(InstanceNode start ,InstanceNode end){
        this.start = start;
        this.end = end;
    }

    public String genAddString(String space){
        String template = "MATCH (f:#space #startCondition), (h:#space #endCondition) with f,h Merge (f)-[:#space]->(h) Merge (f)-[:#defalut_space]->(h)";
        String action = template.replaceAll("#defalut_space" , ModelAssetUtils.DEFAULT_INSTANCE_LABEL)
                .replaceAll("#startCondition" ,start.genIdCondition())
                .replaceAll("#endCondition" ,end.genIdCondition())
                .replaceAll("#space" ,space);
        return action;
    }

    public InstanceNode getStart() {
        return start;
    }

    public InstanceNode getEnd() {
        return end;
    }
}
