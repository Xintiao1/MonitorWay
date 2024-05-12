package cn.mw.monitor.service.model.dto;

@FunctionalInterface
public interface PropertyFilter {
    boolean filter(PropertyInfo propertyInfo);
}
