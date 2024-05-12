package cn.mw.monitor.service.model.dto;

import cn.mw.monitor.service.graph.ModelAssetUtils;
import cn.mw.monitor.service.graph.NodeParam;
import cn.mwpaas.common.utils.StringUtils;

public class InstanceNode {
    private String id;
    private Integer instanceId;

    public InstanceNode(String id) {
        this.id = id;
    }

    public InstanceNode(NodeParam nodeParam) {
        this.id = nodeParam.getId();
        this.instanceId = nodeParam.getRealId();
    }

    public String genCypherString(String space) {
        StringBuffer sb = new StringBuffer("(id").append(this.id)
                .append(":").append(ModelAssetUtils.DEFAULT_INSTANCE_LABEL);
        if (StringUtils.isNotEmpty(space)) {
            sb.append(":").append(space);
        }

        sb.append(" { id :'").append(this.id).append("' } )");
        return sb.toString();
    }

    public String genSetLabel(String space) {
        String template = "match (n:#default_space {id:'#id'}) set n:#space;";
        String action = template.replaceAll("#default_space", ModelAssetUtils.DEFAULT_INSTANCE_LABEL)
                .replaceAll("#id", this.id)
                .replaceAll("#space", space);
        return action;
    }

    public String genCypherString() {
        String template = "(:#space {id:'#id'})";
        String action = template.replaceAll("#space", ModelAssetUtils.DEFAULT_INSTANCE_LABEL)
                .replaceAll("#id", this.id);
        return action;
    }

    public String genIdCondition() {
        String id = "{id :'#id'}".replaceAll("#id", this.id);
        return id;
    }

    public String genIdQuoteString() {
        return "'" + this.id + "'";
    }

    public String getId() {
        return id;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setId(String id) {
        this.id = id;
    }
}
