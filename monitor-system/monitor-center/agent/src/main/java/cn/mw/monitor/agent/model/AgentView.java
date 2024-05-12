package cn.mw.monitor.agent.model;

import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.Data;

import java.util.Map;

@Data
public class AgentView {
    private String ip;
    private int netflowPort;
    private int servicePort;

    public void extractFromInstance(Instance instance){
        this.ip = instance.getIp();
        this.servicePort = instance.getPort();
        Map<String ,String> metaMap = instance.getMetadata();
        String portStr = metaMap.get("agentPort");
        if(StringUtils.isNotEmpty(portStr)){
            this.netflowPort = Integer.parseInt(portStr);
        }
    }

}
