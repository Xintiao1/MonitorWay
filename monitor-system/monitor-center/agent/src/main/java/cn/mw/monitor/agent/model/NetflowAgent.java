package cn.mw.monitor.agent.model;

import cn.mw.monitor.agent.param.NetflowAgentConfigParam;
import lombok.Data;

@Data
public class NetflowAgent {
    private String ip;
    private int port;

    public void extractFromParem(NetflowAgentConfigParam param){
        this.ip = param.getIp();
        this.port = param.getPort();
    }
}
