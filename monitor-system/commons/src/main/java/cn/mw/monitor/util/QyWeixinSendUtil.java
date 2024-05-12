package cn.mw.monitor.util;

import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import cn.mw.monitor.util.entity.GeneralMessageEntity;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;


@Slf4j
public class QyWeixinSendUtil {

    private static String send_qyweixin_message = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=ACCESS_TOKEN";
    private static String qyweixinToken = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ID&corpsecret=SECRET";
    /**
     * @describe 发送企业微信txet消息
     */
    public static String sendQyWeixinMessage(String jsonData, GeneralMessageEntity qiEntity, AlertRuleTableCommons alertRuleTable) {
        String str = send_qyweixin_message.replace("ACCESS_TOKEN", getQyWeixinAccessToken(qiEntity,alertRuleTable));
        log.info("sendQyWeixinMessage str: " + str);
        String result = LoadUtil.post(str, jsonData,alertRuleTable);
        return result;
    }
    /**
     * 向处暴露的获取企业微信token的方法
     *
     * @return
     */
    public static String getQyWeixinAccessToken(GeneralMessageEntity qiEntity,AlertRuleTableCommons alertRuleTable) {
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        boolean isHas = redisUtils.hasKey(qiEntity.getId() + qiEntity.getAgentId() + "QyWeixinAccessToken");
        if (!isHas) {
            getQyWeixinToken(qiEntity,alertRuleTable);
        }
        String token = (String) redisUtils.get(qiEntity.getId() + qiEntity.getAgentId() + "QyWeixinAccessToken");
        log.info("企业微信key:" + qiEntity.getId() + qiEntity.getAgentId() + "QyWeixinAccessToken");
        log.info("企业微信token:" + token);
        return token;
    }
    /**
     * 访问企业微信端获取获取token并且存储起来
     */
    private static void getQyWeixinToken(GeneralMessageEntity qiEntity,AlertRuleTableCommons alertRuleTable) {
        String url = qyweixinToken.replace("ID", qiEntity.getId()).replace("SECRET", qiEntity.getSecret());

        String tokenStr = LoadUtil.get(url,alertRuleTable);
        JSONObject jsonObject = JSONObject.parseObject(tokenStr);
        String token = jsonObject.getString("access_token");
        String expireIn = jsonObject.getString("expires_in");
        log.info("token调用结果返回：" + tokenStr);
        log.info("token调用结果返回token：" + token);
        log.info("token调用结果返回expireIn：" + expireIn);
        //将token放入redis中保存
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        redisUtils.set(qiEntity.getId() + qiEntity.getAgentId() + "QyWeixinAccessToken", token, Long.parseLong(expireIn));
    }

}
