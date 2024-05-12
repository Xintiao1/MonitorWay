package cn.mw.monitor.agent.api;

import cn.mw.monitor.agent.model.AgentView;
import cn.mw.monitor.agent.param.NetFlowConfigParam;
import java.util.List;

public interface AgentManage {
    boolean updateFilterRule(FilterRuleChange filterRuleChange) throws Exception;
    boolean netflowConfig(NetFlowConfigParam netFlowConfigParam);
    List<AgentView> getAgentViews();
}
