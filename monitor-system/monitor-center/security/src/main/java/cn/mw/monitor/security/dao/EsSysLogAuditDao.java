package cn.mw.monitor.security.dao;

import cn.mw.module.security.dto.DataSourceConfigureDTO;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/12/27
 */
public interface EsSysLogAuditDao {
    List<Map<String,Object>> getAllAssetsInfoByIp();

    List<DataSourceConfigureDTO> initDataSourceState();
}
