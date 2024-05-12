package cn.mw.module.security.service;

import cn.mw.module.security.dto.DataSourceConfigureDTO;
import cn.mw.monitor.security.dao.DataSourceConfigureDao;

import java.util.List;

public interface OperateDataSource {
    /**
     * 数据源创建
     * @param dataSourceConfigureDao
     * @param dataSource
     */
    Integer creatDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource);
    /**
     * 处理数据源信息
     * @param
     * @return
     */
    DataSourceConfigureDTO getDataSourceInfo(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource);
    /**
     * 数据源编辑
     * @param dataSourceConfigureDao
     * @param dataSource
     */
    Integer editDataSourceInfo(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource);

    /**
     * 关闭数据源启动状态
     * @param dataSourceConfigureDao
     * @param dataSource
     */
    Integer shutDownDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource);

    /**
     * 开启数据源启动状态
     * @param dataSourceConfigureDao
     * @param dataSource
     */
    Integer initDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource);
}
