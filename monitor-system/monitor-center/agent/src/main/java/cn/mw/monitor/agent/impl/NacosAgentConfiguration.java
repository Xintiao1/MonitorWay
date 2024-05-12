package cn.mw.monitor.agent.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NacosAgentConfiguration {

    @Bean
    public NacosAllInstanceClient createAllInstancClient(NacosAgentManage nacosAgentManage){
        return new NacosAllInstanceClient(nacosAgentManage);
    }
}
