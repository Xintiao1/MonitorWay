package cn.mw.monitor.screen.service.impl;

import cn.mw.monitor.screen.model.MapAlert;

import java.util.List;

public interface FinishProcessCallBack {
    void finish(List<MapAlert> mapAlertList);
}
