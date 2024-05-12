package cn.mw.monitor.agent.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AgentConfigModel {
    NetflowConfigModel netflow;

    public void init(){
        netflow = new NetflowConfigModel();
        List<NetflowAgent> netflowAgents = new ArrayList<>();
        netflow.setAgentList(netflowAgents);
    }

    public void writeFilterRule(String rule){
        if(null == netflow){
            netflow = new NetflowConfigModel();
        }

        FilterRuleModel filterRuleModel = netflow.getFilterRule();
        if(null == filterRuleModel){
            filterRuleModel = new FilterRuleModel();
            netflow.setFilterRule(filterRuleModel);
        }

        filterRuleModel.setData(rule);
    }

    public void addNetflowAgent(NetflowAgent netflowAgent){
        List<NetflowAgent> list = netflow.getAgentList();
        if(null == list){
            list = new ArrayList<NetflowAgent>();
            netflow.setAgentList(list);
        }
        list.add(netflowAgent);
    }
}
