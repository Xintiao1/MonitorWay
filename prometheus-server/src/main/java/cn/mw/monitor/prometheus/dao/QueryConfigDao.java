package cn.mw.monitor.prometheus.dao;

import cn.mw.monitor.prometheus.dto.PanelConfigDto;
import cn.mw.monitor.prometheus.dto.QueryConfigDto;

import java.util.List;

public interface QueryConfigDao {

    List<QueryConfigDto> getAllQueryConfigs();

    QueryConfigDto getQueryConfigById(Integer queryId);
}
