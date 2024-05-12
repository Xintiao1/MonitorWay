package cn.mw.monitor.neo4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfiguration {

    @Value("${mw.neo4j.clientUri}")
    private String url;

    @Value("${mw.neo4j.user}")
    private String user;

    @Value("${mw.neo4j.password}")
    private String password;

    @Value("${mw.neo4j.initSize}")
    private int initSize;

    @Value("${mw.graph.enable}")
    private boolean graphEnable;

    @Bean
    public ConnectionPool genConnectionPool(){
        if(!graphEnable){
            return null;
        }
        return new ConnectionPool(url ,user ,password ,initSize);
    }
}
