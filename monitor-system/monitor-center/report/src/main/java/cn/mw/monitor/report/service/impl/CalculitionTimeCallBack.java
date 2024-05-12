package cn.mw.monitor.report.service.impl;

import java.util.List;

public interface CalculitionTimeCallBack {
    List<Long> calculitionTime(DateTypeEnum dateTypeEnum, List<String> chooseTime);
}
