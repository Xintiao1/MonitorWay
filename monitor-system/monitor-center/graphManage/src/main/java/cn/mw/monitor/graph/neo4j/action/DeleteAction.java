package cn.mw.monitor.graph.neo4j.action;


import cn.mwpaas.common.utils.StringUtils;

public class DeleteAction {
    private String label;
    private String relation;
    private String nodeCondition;

    public String getCommand(){
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer del = new StringBuffer();
        stringBuffer.append("match (n:`").append(label).append("`)");

        del.append(" delete n");
        if(StringUtils.isNotEmpty(relation)){
            stringBuffer.append("-[r:").append(relation).append("]").append("-()");
            del.append(" ,r");
        }

        if(StringUtils.isNotEmpty(nodeCondition)){
            stringBuffer.append(" where (n.").append(nodeCondition).append(") ");
        }

        stringBuffer.append(del);
        return stringBuffer.toString();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNodeCondition() {
        return nodeCondition;
    }

    public void setNodeCondition(String nodeCondition) {
        this.nodeCondition = nodeCondition;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
