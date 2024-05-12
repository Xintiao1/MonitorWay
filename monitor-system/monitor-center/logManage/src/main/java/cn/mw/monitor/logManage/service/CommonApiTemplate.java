package cn.mw.monitor.logManage.service;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.utils.OkHttpUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class CommonApiTemplate extends AbstractApiTemplate {
    @Override
    public ResponseBase executeApi(String url, Object o) {
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(o));

        return OkHttpUtils.getResultObject(result);
    }
}
