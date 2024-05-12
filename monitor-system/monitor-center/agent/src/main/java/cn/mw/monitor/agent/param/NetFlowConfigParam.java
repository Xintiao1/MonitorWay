package cn.mw.monitor.agent.param;

import lombok.Data;

import java.util.List;

@Data
public class NetFlowConfigParam {
    private Long saveDays;
    private List<NetflowAgentConfigParam> agentParam;
}
