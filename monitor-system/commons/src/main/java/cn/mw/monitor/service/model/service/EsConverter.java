package cn.mw.monitor.service.model.service;

@FunctionalInterface
public interface EsConverter {
    Object convertToESData(String value);
}
