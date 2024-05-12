package cn.mw.monitor.agent.model;

import lombok.Data;

import java.util.List;

@Data
public class NetflowConfigModel {
    FilterRuleModel filterRule;
    List<NetflowAgent> agentList;
}
