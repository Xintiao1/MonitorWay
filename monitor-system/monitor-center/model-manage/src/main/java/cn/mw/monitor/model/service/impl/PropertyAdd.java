package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.PropertyInfo;

import java.util.List;

@FunctionalInterface
public interface PropertyAdd {
    List<PropertyInfo> add(ModelInfo modelInfo);
}
