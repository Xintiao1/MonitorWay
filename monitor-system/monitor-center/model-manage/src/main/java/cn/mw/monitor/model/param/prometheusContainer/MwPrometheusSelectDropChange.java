package cn.mw.monitor.model.param.prometheusContainer;


public interface MwPrometheusSelectDropChange {
    String getType();

    Object getData(Object data) throws Exception;
}
