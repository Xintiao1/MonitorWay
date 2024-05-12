package cn.mw.monitor.logManage.service.impl;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.constant.LogManageConstant;
import cn.mw.monitor.logManage.param.VectorChannelParam;
import cn.mw.monitor.logManage.param.VectorChannelSearchParam;
import cn.mw.monitor.logManage.service.MwVectorChannelService;
import cn.mw.monitor.logManage.utils.OkHttpUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MwVectorChannelServiceImpl implements MwVectorChannelService {

    private static final String URL_PREFIX = LogManageConstant.VECTOR_CHANNEL_URL_PREFIX;

    @Override
    public ResponseBase selectList(VectorChannelSearchParam searchParam) {
        String url = URL_PREFIX + "list";
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(searchParam));

        return OkHttpUtils.getResultObject(result);
    }



    @Override
    public ResponseBase addVectorChannel(VectorChannelParam param) {
        String url = URL_PREFIX + "add";
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(param));
        return OkHttpUtils.getResultObject(result);
    }

    @Override
    public ResponseBase updateVectorChannel(VectorChannelParam param) {
        String url = URL_PREFIX + "update";
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(param));
        return OkHttpUtils.getResultObject(result);
    }

    @Override
    public ResponseBase deleteVectorChannel(List<Integer> ids) {
        String url = URL_PREFIX + "delete";
        String result = OkHttpUtils.httpPostJson(url, JSONObject.toJSONString(ids));
        return OkHttpUtils.getResultObject(result);

    }
}
