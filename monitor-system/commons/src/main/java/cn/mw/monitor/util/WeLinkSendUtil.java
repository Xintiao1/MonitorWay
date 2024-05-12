package cn.mw.monitor.util;

import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.alert.dto.WeLinkRuleParam;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.*;

@Slf4j
public class WeLinkSendUtil {

    public static void getToken(String appId,  String appSecret, String type) throws IOException{

        String url = "https://open.welink.huaweicloud.com/api/auth/v2/tickets";
        Map<String, Object> param = new HashMap<>();
        param.put("client_id", appId);
        param.put("client_secret", appSecret);
        param.put("type", type);
        param.put("code", "");
        String result = DingdingQunSendUtil.sendPostByMap(url,param);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String token = jsonObject.getString("access_token");
        String expires = jsonObject.getString("expires_in");
        //将token放入redis中保存
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        redisUtils.set("WeLinkAccessToken", token, Long.parseLong(expires));
    }

    public static String getAccessToken(String appId,  String appSecret, String type) throws IOException {
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        boolean isHas = redisUtils.hasKey("WeLinkAccessToken");
        log.info("这是个微信调用日志："+isHas);
        if (!isHas) {
            getToken(appId,appSecret,type);
        }
        String token = (String) redisUtils.get("WeLinkAccessToken");
        return token;
    }

    public static String send(Map<String, Object> param, WeLinkRuleParam weLinkRuleParam) throws IOException {
        String token = getAccessToken(weLinkRuleParam.getAppId(),weLinkRuleParam.getAppSecret(),"u");
        String url = "https://open.welink.huaweicloud.com/api/messages/v2/send";
        Map<String, String> headParam = new HashMap();
        headParam.put("Content-type", "application/json;charset=UTF-8");
        headParam.put("x-wlk-Authorization", token);
        String result = DingdingQunSendUtil.doPost(url,param,headParam);
        return result;
    }

    public static List<String> getUserId(HashSet<String> phones, WeLinkRuleParam weLinkRuleParam) throws IOException {
        String url = "https://open.welink.huaweicloud.com/api/contact/v2/users/detail";
        String token = getAccessToken(weLinkRuleParam.getAppId(),weLinkRuleParam.getAppSecret(),"u");
        Map<String, String> headParam = new HashMap();
        headParam.put("Content-type", "application/json;charset=UTF-8");
        headParam.put("x-wlk-Authorization", token);
        List<String> userIds = new ArrayList<>();
        for(String phone : phones){
            Map<String, Object> param = new HashMap<>();
            param.put("mobileNumber",phone);
            String result = DingdingQunSendUtil.doPost(url,param,headParam);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if(jsonObject.getString("code").equals("0")){
                String userId = jsonObject.getString("userId");
                userIds.add(userId);
            }
        }
        return userIds;
    }

}
