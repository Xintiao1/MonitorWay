package cn.mw.module.security.util;

import cn.mw.module.security.dto.DataSourceConfigureDTO;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * es配置
 *
 * @author qzg
 * @date 2021/12/13
 */
public class ElasticsearchConfig {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    private static int threadNum = 6; //线程数
    private static int connectTimeOut = 10000; // 连接超时时间
    private static int socketTimeOut = 30000; // 连接超时时间
    private static int connectionRequestTimeOut = 10000; // 获取连接的超时时间

    private static int maxConnectNum = 4000; // 最大连接数
    private static int maxConnectPerRoute = 1000; // 最大路由连接数

    public static RestHighLevelClient getRestHighLevelClient(DataSourceConfigureDTO connect) {
        String connectTypeName = connect.getConnectionTypeName();
        String ip = connect.getIp();
        Integer port = connect.getPort();
        String userName = connect.getUserName();
        String password = connect.getPassword();
        //是否认证  TRUE认证，需要用户名密码  false 直接连接
        Boolean isPass = connect.getIsPass();

        logger.info(connectTypeName + ":\\\\" + ip + ":" + port + ";");

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        RestClientBuilder builder = RestClient.builder(new HttpHost(ip, port, connectTypeName));
        //账号认证
        if(isPass){
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
        }
        // 异步httpclient连接延时配置
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(connectTimeOut);
                requestConfigBuilder.setSocketTimeout(socketTimeOut);
                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
                return requestConfigBuilder;
            }
        });
        // 异步httpclient连接数配置
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnTotal(maxConnectNum);
                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(threadNum).build());
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                return httpClientBuilder;
            }
        });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }


//    public static RestHighLevelClient getRestHighLevelClient(EsDataSourceConfigureDTO connect) {
////        String[] hosts = connect.getIp().split(",");
//////        HttpHost[] httpHosts = new HttpHost[hosts.length];
//////        for (int i = 0; i < hosts.length; i++) {
//////            httpHosts[i] = HttpHost.create(hosts[i]);
//////        }
//        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(connect.getIP(),connect.getPort()));
//        //配置身份验证
//        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(connect.getUserName(), connect.getPassword()));
//        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
//        return new RestHighLevelClient(restClientBuilder);
//    }


}
