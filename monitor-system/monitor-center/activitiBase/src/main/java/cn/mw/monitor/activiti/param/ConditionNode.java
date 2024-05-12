package cn.mw.monitor.activiti.param;

import java.util.ArrayList;
import java.util.List;

public class ConditionNode {
    private String nodeName;
    private boolean error;
    private int type;
    private int priorityLevel;
    private ConditionNode childNode;
    private List<Condition> conditionList = new ArrayList<>();
    private List<NodeUser> nodeUserList = new ArrayList<>();
}
