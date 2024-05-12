package cn.mw.monitor.logManage.service.impl;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.constant.LogManageConstant;
import cn.mw.monitor.logManage.param.ForWardAddParam;
import cn.mw.monitor.logManage.param.MwForwardSearchParam;
import cn.mw.monitor.logManage.service.MwForWardService;
import cn.mw.monitor.logManage.utils.OkHttpUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MwForWardServiceImpl implements MwForWardService {

    private static final String URL_PREFIX = LogManageConstant.VECTOR_FORWARD_URL_PREFIX;

    @Override
    public ResponseBase addForWard(ForWardAddParam param) {
        String url = URL_PREFIX + "add";
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(param));
        return OkHttpUtils.getResultObject(result);
    }

    @Override
    public ResponseBase updateForWard(ForWardAddParam param) {
        String url = URL_PREFIX + "update";
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(param));
        return OkHttpUtils.getResultObject(result);
    }

    @Override
    public ResponseBase deleteByIds(List<Integer> ids) {
        String url = URL_PREFIX + "delete";
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(ids));
        return OkHttpUtils.getResultObject(result);
    }

    @Override
    public ResponseBase pageList(MwForwardSearchParam searchParam) {
        String url = URL_PREFIX + "list";
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(searchParam));

        return OkHttpUtils.getResultObject(result);
    }

    @Override
    public ResponseBase getEnumValue() {
        String url = URL_PREFIX + "getEnumValue";
        String result = OkHttpUtils.httpPostJson(url, "");

        return OkHttpUtils.getResultObject(result);
    }


}
