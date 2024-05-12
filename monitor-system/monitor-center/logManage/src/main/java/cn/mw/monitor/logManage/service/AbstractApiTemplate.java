package cn.mw.monitor.logManage.service;

import cn.mw.monitor.api.common.ResponseBase;

public abstract class AbstractApiTemplate {
    public abstract ResponseBase executeApi(String url, Object o);
}
