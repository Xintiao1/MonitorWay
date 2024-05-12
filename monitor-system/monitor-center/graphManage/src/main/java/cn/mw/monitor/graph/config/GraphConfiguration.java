package cn.mw.monitor.graph.config;

import cn.mw.monitor.graph.GraphContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphConfiguration {

    @Bean
    public GraphContext createGraphContext(){
        return new GraphContext();
    }
}
