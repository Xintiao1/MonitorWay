package cn.mw.module.security.service.impl;

import cn.mw.module.security.dto.DataSourceConfigureDTO;
import cn.mw.module.security.service.OperateDataSource;
import cn.mw.monitor.security.dao.DataSourceConfigureDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.Socket;

/**
 * es数据源操作
 */
@Component
@Slf4j
public class OperateClickHouseDataSource implements OperateDataSource {
    /*@Resource
    private DataSourceConfigureDao dataSourceConfigureDao;*/

    @Override
    public Integer creatDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        return dataSourceConfigureDao.creatDataSourceInfo(dataSource);
    }

    @Override
    public DataSourceConfigureDTO getDataSourceInfo(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        //监测服务运行状态
        if(clickHouseAlive(dataSource.getIp(),dataSource.getPort(),dataSource.getUserName(),dataSource.getPassword())){
            //服务正常
            dataSource.setStatus(1);
            dataSourceConfigureDao.editorDataSourceStatus(dataSource);
        } else {
            //服务异常
            dataSource.setStatus(0);
            dataSourceConfigureDao.editorDataSourceStatus(dataSource);
        }
       return dataSource;
    }

    @Override
    public Integer editDataSourceInfo(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        return dataSourceConfigureDao.editorDataSourceInfo(dataSource);
    }

    @Override
    public Integer shutDownDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        return dataSourceConfigureDao.editorDataSourceInfo(dataSource);
    }

    @Override
    public Integer initDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        return dataSourceConfigureDao.editorDataSourceInfo(dataSource);
    }

    public boolean clickHouseAlive(String host, int port, String username, String password) {
        try (Socket socket = new Socket(host, port)) {
            // 连接成功，说明ClickHouse已启用
            return true;
        } catch (Exception e) {
            // 连接失败，说明ClickHouse未启用或连接配置有误
            return false;
        }
    }
}
