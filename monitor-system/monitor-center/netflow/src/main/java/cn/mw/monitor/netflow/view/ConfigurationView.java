package cn.mw.monitor.netflow.view;


import cn.mw.monitor.agent.model.AgentView;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ConfigurationView {
    private Map<String ,String> configMap;
    private List<AgentView> agentViewList;

    public void addAgentView(AgentView agentView){
        if(null == agentViewList){
            agentViewList = new ArrayList<>();
        }
        agentViewList.add(agentView);
    }
}
