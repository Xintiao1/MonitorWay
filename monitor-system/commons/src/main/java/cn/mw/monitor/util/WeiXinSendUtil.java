package cn.mw.monitor.util;

import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WeiXinSendUtil {



    public static String getAccessToken(String appid, String secret) {
        return getAccessToken(appid,secret,null);
    }

    /**
     * 获取token的方法
     *
     * @return
     */
    public static String getAccessToken(String appid, String secret, AlertRuleTableCommons alertRuleTable) {
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        boolean isHas = redisUtils.hasKey("AccessToken");
        log.info("这是个微信调用日志："+isHas);
        if (!isHas) {
            getToken(appid,secret,alertRuleTable);
        }
        String token = (String) redisUtils.get("AccessToken");
        log.info("这是个微信调用日志 token："+token);
        return token;
    }

    /**
     * 访问微信端获取获取token并且存储起来
     */
    private static void getToken(String appid, String secret, AlertRuleTableCommons alertRuleTable) {
        log.info("这是个微信调用日志 getToken方法Start");
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                "&appid=" +appid.trim()+
                "&secret=" +secret.trim();
        String tokenStr = LoadUtil.get(url,alertRuleTable);
        JSONObject jsonObject = JSONObject.parseObject(tokenStr);
        String token = jsonObject.getString("access_token");
        String expireIn = jsonObject.getString("expires_in");

        //将token放入redis中保存
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        redisUtils.set("AccessToken", token, Long.parseLong(expireIn));
        log.info("这是个微信调用日志 getToken方法end");
    }

    public static void send(String touser, HashMap<String, String> wxmap, String token) throws IOException {
        send(touser, wxmap, token, null);
    }

    /**
     * 微信发送
     */
    public static void send(String touser, HashMap<String, String> wxmap, String token, AlertRuleTableCommons alertRuleTable) throws IOException {
        log.info("weixin star touser:" + touser);
        log.info("微信 wxmap:" + wxmap);
        log.info("微信 appid:" + wxmap.get("appid"));
        log.info("微信 secret:" + wxmap.get("secret"));
        log.info("微信 templateid:" + wxmap.get("templateid"));
        //2,推送消息
        // 接口地址
        String sendMsgApi = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+token;
        //整体参数map
        Map<String, Object> paramMap = new HashMap<String, Object>();
        //消息主题显示相关map
        Map<String, Object> dataMap = new HashMap<String, Object>();
        //根据自己的模板定义内容和颜色
        JSONObject first = new JSONObject();
        first.put("value",wxmap.get("first"));
        JSONObject keyword1 = new JSONObject();
        keyword1.put("value",wxmap.get("keyword1"));
        JSONObject keyword2 = new JSONObject();
        keyword2.put("value",wxmap.get("keyword2"));
        JSONObject keyword3 = new JSONObject();
        keyword3.put("value",wxmap.get("keyword3"));
        JSONObject keyword4 = new JSONObject();
        keyword4.put("value",wxmap.get("keyword4"));
        JSONObject keyword5 = new JSONObject();
        keyword5.put("value",wxmap.get("keyword5"));
        JSONObject remark = new JSONObject();
        remark.put("value",wxmap.get("remark"));

        dataMap.put("first",first);
        dataMap.put("keyword1",keyword1);
        dataMap.put("keyword2",keyword2);
        dataMap.put("keyword3",keyword3);
        dataMap.put("keyword4",keyword4);
        dataMap.put("keyword5",keyword5);
        dataMap.put("remark",remark);
        paramMap.put("touser", touser);
        paramMap.put("template_id", wxmap.get("templateid"));
        paramMap.put("data", dataMap);
        try {
            String result = doGetPost(sendMsgApi,"POST",paramMap,alertRuleTable);
            log.info("微信发送结果：" + result);
        } catch (Exception e) {
            log.error("微信推送失败：", e);
        }
        //微信推送（无需模板，但是需要用户48小时内有交互）
        /*Map<String,Object> json = new HashMap();
        Map<String,Object> text = new HashMap();
        json.put("msgtype","text");
        json.put("touser",touser);
        text.put("content",content);
        json.put("text",text);
        //获取token
        String token = getAccessToken();
        log.info("wxsendmessage token" + token);
        String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + token;
        log.info("weixin:{} url",url);
        log.info("weixin:{} json",json);
        String response = sendPostByMap(url, json);
        log.info("wxsendmessage response:" + response);
        Integer errcode = JSONObject.parseObject(response).getInteger("errcode");
        log.info("wxsendmessage errcode:" + errcode);
        return errcode;*/
    }

    public static String doGetPost(String apiPath,String type,Map<String,Object> paramMap){
        return doGetPost(apiPath,type,paramMap,null);
    }

    /**
     * 调用接口 post
     * @param apiPath
     */
    public static String doGetPost(String apiPath,String type,Map<String,Object> paramMap,AlertRuleTableCommons alertRuleTable){
        OutputStreamWriter out = null;
        InputStream is = null;
        String result = null;
        try{
            Proxy proxy = null;
            if(alertRuleTable!=null && alertRuleTable.getProxyState() != null){
                proxy = LoadUtil.getProxy(alertRuleTable);
            }
            URL url = new URL(apiPath);// 创建连接
            HttpURLConnection connection = null;
            if(proxy != null){
                connection = (HttpURLConnection) url.openConnection(proxy);
            }else{
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod(type) ; // 设置请求方式
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.connect();
            if(paramMap != null){
                out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
                out.append(JSON.toJSONString(paramMap));
                out.flush();
                out.close();
            }
            // 读取响应
            is = connection.getInputStream();
            int length = (int) connection.getContentLength();// 获取长度
            if (length != -1) {
                byte[] data = new byte[length];
                byte[] temp = new byte[512];
                int readLen = 0;
                int destPos = 0;
                while ((readLen = is.read(temp)) > 0) {
                    System.arraycopy(temp, 0, data, destPos, readLen);
                    destPos += readLen;
                }
                result = new String(data, "UTF-8"); // utf-8编码
            }
        } catch (IOException e) {
            log.error("doGetPost:{}",e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                log.error("doGetPost IOException:{}",e);
            }
        }
        return  result;
    }


}
