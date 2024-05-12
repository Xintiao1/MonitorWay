package cn.mw.monitor.prometheus.dao;

import cn.mw.monitor.prometheus.dto.PrometheusPropertyDto;

import java.util.List;

public interface PrometheusPropertyDao {

    List<PrometheusPropertyDto> getAllPrometheusProperties();

}
