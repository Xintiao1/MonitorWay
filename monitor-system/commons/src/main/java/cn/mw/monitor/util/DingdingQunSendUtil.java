package cn.mw.monitor.util;

import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.api.common.SpringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DingdingQunSendUtil {

    public static String sendMessage(String sendMessage, String webHook, String secret) {
        String response = null;
        String url = webHook;
        try {
            /*String res = wxPortalService.sendQyWeixinMessage(sendMessage,qyEntity);*/
            //判断是否勾选了签名
            if(!secret.equals("")){
                Long timestamp = System.currentTimeMillis();
                String sign = signatureCalculation(secret,timestamp);
                url = webHook
                        + "&timestamp=" + timestamp
                        + "&sign="+ sign;
            }
            Map<String,Object> json=new HashMap();
            Map<String,Object> text=new HashMap();
            Map<String,Object> at=new HashMap();
            //Map<String,List<String>> atMobiles = new HashMap();
            //List mob = new ArrayList<String>();
            //mob.add("");//艾特人的手机号
            json.put("msgtype","text");
            text.put("content",replace(sendMessage));
            //atMobiles.put("atMobiles", mob);//艾特人的手机号
            at.put("isAtAll", true);//是否艾特所有人
            //at.put("atMobiles", mob);
            json.put("text",text);
            json.put("at",at);
            // 发送post请求
            response = sendPostByMap(url, json);

        }catch (Exception e){
            response = ("发送信息异常");
            log.error("error perform send message dingdingqun:",e);
        }
        return response;
    }
    /*
     *签名计算
     */
    public static String signatureCalculation(String secret, Long timestamp) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
        return sign;
    }

    public static String replace(String s){
        return s.replace("\"","");
    }

    /**
     * 发送POST请求，参数是Map, contentType=x-www-form-urlencoded
     *
     */
    public static String sendPostByMap(String url, Map<String, Object> mapParam) throws IOException {
        Map<String, String> headParam = new HashMap();
        headParam.put("Content-type", "application/json;charset=UTF-8");
        return  doPost(url,mapParam,headParam);
    }

    public static String sendPostByMap(String url, Map<String, Object> param, Map<String, String> header) throws IOException {
        header.put("Content-type", "application/json;charset=UTF-8");
        return  doPost(url,param,header);
    }

    public static String doPost(String url, Map<String, Object> param, Map<String, String> header) throws IOException {
        // 创建Httpclient对象
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httppost.addHeader(entry.getKey(), entry.getValue());
            }
        }
         StringEntity se = new StringEntity(JSON.toJSONString(param), "utf-8");
         httppost.setEntity(se);

        HttpResponse response = httpclient.execute(httppost);
        String result = null;
        result = EntityUtils.toString(response.getEntity());
        return result;
    }

}
