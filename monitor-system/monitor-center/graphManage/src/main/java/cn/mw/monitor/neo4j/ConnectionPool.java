package cn.mw.monitor.neo4j;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

public class ConnectionPool {

    private SessionFactory sessionFactory;

    public ConnectionPool(String url ,String user ,String password ,int initSize){
        String[] packages = new String[3];
        packages[0] = "cn.mw.monitor.graph.topo";
        packages[1] = "cn.mw.monitor.graph.neo4j";
        packages[2] = "cn.mw.monitor.graph.modelAsset";

        Configuration config = new Configuration.Builder()
                .uri(url)
                .credentials(user, password)
                .connectionPoolSize(initSize)
                .build();
        this.sessionFactory = new SessionFactory(config ,packages);
    }

    //获取连接
    public Session getSession() throws Exception {
        return sessionFactory.openSession();
    }
}
