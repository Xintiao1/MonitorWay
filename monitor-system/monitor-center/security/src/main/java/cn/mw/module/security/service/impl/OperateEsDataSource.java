package cn.mw.module.security.service.impl;

import cn.mw.module.security.dto.DataSourceConfigureDTO;
import cn.mw.module.security.dto.EsDataSourceListDto;
import cn.mw.module.security.dto.EsDataSourceListInfoDto;
import cn.mw.module.security.service.OperateDataSource;
import cn.mw.module.security.util.ElasticsearchConfig;
import cn.mw.monitor.security.dao.DataSourceConfigureDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * es数据源操作
 */
@Component
@Slf4j
public class OperateEsDataSource implements OperateDataSource {
    /*@Resource
    private DataSourceConfigureDao dataSourceConfigureDao;*/

    @Override
    public Integer creatDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        return dataSourceConfigureDao.creatDataSourceInfo(dataSource);
    }

    @Override
    public DataSourceConfigureDTO getDataSourceInfo(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        //监测服务运行状态
        if(esAlive(dataSource.getIp(),dataSource.getPort(),dataSource.getUserName(),dataSource.getPassword())){
            //服务正常
            dataSource.setStatus(1);
            dataSourceConfigureDao.editorDataSourceStatus(dataSource);
        } else {
            //服务异常
            dataSource.setStatus(0);
            dataSourceConfigureDao.editorDataSourceStatus(dataSource);
        }
        log.info("服务运行状态监测完成"+dataSource.getIp());
        //监测服务启动状态
        //获取RestHighLevelClient的全局变量信息
        EsDataSourceListDto infoList = new EsDataSourceListDto();
        try {
            //关闭选择的RestHighLevelClient
            //  日志管理那边，每次查询时都要获取一下数据源的信息，之前没有保存到redis中取，后面要修改从数据库中获取，自动连接。
            if (infoList != null && infoList.getInfoList() != null && infoList.getInfoList().size() > 0) {
                Iterator<EsDataSourceListInfoDto> iterator = EsDataSourceListDto.infoList.iterator();
                while (iterator.hasNext()) {
                    EsDataSourceListInfoDto next = iterator.next();
                    if(next.getId().equals(dataSource.getId())){
                        dataSource.setState(1);
                    }
                    //状态改为 启用
                    dataSourceConfigureDao.editorDataSourceState(dataSource);
                }
            }else{
                //状态改为 禁用
                dataSource.setState(0);
                dataSourceConfigureDao.editorDataSourceState(dataSource);
            }
        } catch (Exception e) {
            log.error("fail to getDataSourceInfo:{} cause:{}", e);
        }
       return dataSource;
    }

    @Override
    public Integer editDataSourceInfo(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        return dataSourceConfigureDao.editorDataSourceInfo(dataSource);
    }

    @Override
    public Integer shutDownDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        EsDataSourceListDto infoList = new EsDataSourceListDto();
        //状态改为 禁用
        int i = dataSourceConfigureDao.editorDataSourceInfo(dataSource);
        try {
            //关闭选择的RestHighLevelClient
            if (infoList != null && infoList.getInfoList() != null && infoList.getInfoList().size() > 0) {
                Iterator<EsDataSourceListInfoDto> iterator = EsDataSourceListDto.infoList.iterator();
                while (iterator.hasNext()) {
                    EsDataSourceListInfoDto next = iterator.next();
                    if (dataSource.getId().equals(next.getId())) {
                        //todo 关闭es连接使用
                        next.getClient().close();
                        iterator.remove();
                    }
                }
            }
        } catch (IOException e) {
            log.error("fail to shutDownConfig param{}, case by {}", dataSource, e);
            return 0;
        }
        return i;
    }

    @Override
    public Integer initDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        List<DataSourceConfigureDTO> list = new ArrayList<>();
        try {
            EsDataSourceListDto infoList = new EsDataSourceListDto();
            List<EsDataSourceListInfoDto> clientInfoList = new ArrayList<>();
            dataSource.setState(null);
            list = dataSourceConfigureDao.getDataSourceInfo(dataSource);

            //每次应用前，先关闭之前连接的RestHighLevelClient
            if (infoList != null && infoList.getInfoList() != null && infoList.getInfoList().size() > 0) {
                for (EsDataSourceListInfoDto p : EsDataSourceListDto.infoList) {
                    p.getClient().close();
                }
                infoList.getInfoList().clear();
            }
            //状态为启用
            dataSource.setState(1);
            List<RestHighLevelClient> restHighLevelClientList = new ArrayList<>();
            if (list != null && list.size() > 0) {
                for (DataSourceConfigureDTO dto : list) {
                    //todo es连接实例
                    EsDataSourceListInfoDto clientInfo = new EsDataSourceListInfoDto();
                    RestHighLevelClient restHighLevelClient = null;
                    restHighLevelClient = ElasticsearchConfig.getRestHighLevelClient(dto);
                    dto.setState(1);
                    dataSourceConfigureDao.editorDataSourceInfo(dto);
                    restHighLevelClientList.add(restHighLevelClient);
                    clientInfo.setClient(restHighLevelClient);
                    clientInfo.setQueryEsIndex(dto.getQueryEsIndex());
                    clientInfo.setId(dto.getId());
                    clientInfo.setDataSourceName(dto.getDataSourceName());
                    clientInfo.setDataSourceType(dto.getDataSourceTypeName());
                    clientInfoList.add(clientInfo);
                }
            }
            infoList.setInfoList(clientInfoList);
        } catch (Exception e) {
            log.error("fail to initConfig:{} cause:{}", e);
            return 0;
        }
        return list.size();
    }

    public boolean esAlive(String host,int port, String username,String password){
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(host, port, "http"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);
        try {
            ClusterHealthRequest request = new ClusterHealthRequest();
            ClusterHealthResponse response = restHighLevelClient.cluster().health(request, RequestOptions.DEFAULT);
            if (response.getStatus() != ClusterHealthStatus.RED) {
                return true;
            }
        }
        catch (IOException e) {
            log.error("检查ES集群运行状况时出错:{}"+e);
        } finally {
            if (restHighLevelClient != null) {
                try { restHighLevelClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

}
