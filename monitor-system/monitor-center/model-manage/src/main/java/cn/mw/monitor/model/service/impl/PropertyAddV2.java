package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.ModelInfoV2;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.dto.PropertyInfoV2;

import java.util.List;

@FunctionalInterface
public interface PropertyAddV2 {
    List<PropertyInfoV2> add(ModelInfoV2 modelInfo);
}
