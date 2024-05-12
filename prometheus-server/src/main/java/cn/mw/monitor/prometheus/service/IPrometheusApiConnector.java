package cn.mw.monitor.prometheus.service;

import cn.mw.monitor.prometheus.vo.PanelQueryParamVo;
import cn.mw.monitor.prometheus.vo.PrometheusResponseVo;

import java.util.List;

public interface IPrometheusApiConnector {

    PrometheusResponseVo doQuery(PanelQueryParamVo panelQueryParamVo);

    PrometheusResponseVo doQueryRange(PanelQueryParamVo panelQueryParamVo);
}
