package cn.mw.monitor.weixin.service.impl;

import cn.mw.monitor.alert.param.DingDingQunParam;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.weixin.entity.DingdingqunFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

/**
 * 钉钉群机器人发送实现类
 */
public class DingdingQunSendMessageiImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    //发送方（钉钉群机器人）
    private DingdingqunFromEntity qyEntity;



    public DingdingQunSendMessageiImpl(HashMap<String, String> map, HashSet<Integer> userIds,
                                       HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) throws Exception {
        log.info("钉钉群map：" + map);
        this.map = map;
        this.userIds = userIds;
        this.severity = severity;
        this.assets = assets;
        this.ruleId = ruleId;
        this.isAlarm = map.get("告警标题")==null? map.get("恢复标题")==null? null:false : true;

    }

    public static String doPost(String url, Map<String, Object> param, Map<String, String> header) throws IOException {
        // 创建Httpclient对象
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        StringEntity se = new StringEntity(JSON.toJSONString(param), "utf-8");
        httppost.setEntity(se);
        HttpResponse response = httpclient.execute(httppost);
        String result = null;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            result = EntityUtils.toString(response.getEntity());
        }
        return result;
    }


    @Override
    public void sendMessage(String sendMessage) {
        Integer errcode = -1;
        String erroMessage = "";
        try {
            String response = DingdingQunSendUtil.sendMessage(sendMessage,qyEntity.getWebHook(),qyEntity.getSecret());
            errcode = 0;
            log.info("钉钉群发送结果：" + response);
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.error("error perform send message dingdingqun:{}",e);
        }finally {
            saveHis("钉钉群",sendMessage,errcode,map.get("事件ID"),erroMessage,this.title,map.get("IP地址"),isAlarm, userIds,map.get(AlertEnum.HOSTID.toString()));
        }
    }
    public String replace(String s){
        return s.replace("\"","");
    }
    /*
    *签名计算
    *
    */
    public String signatureCalculation(String secret,Long timestamp) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
        return sign;
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

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String, Object> param, Map<String, String> headParam) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "-1";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性 请求头
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Fiddler");

            if (headParam != null) {
                for (Entry<String, String> entry : headParam.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(JSON.toJSONString(param));
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            log.error("失败:{}",e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public String dealMessage() {
        return super.dealMessage();
    }


    @Override
    public Object selectFrom(){
        DingdingqunFromEntity qyEntity = mwWeixinTemplateDao.findDingdingQunMessage(ruleId);
        decrypt(qyEntity);
        this.qyEntity = qyEntity;
        return qyEntity;
    }
    public  void decrypt(DingdingqunFromEntity dingDingQun) {
        if(dingDingQun != null){
            try {
                if (dingDingQun.getWebHook() != null) {
                    dingDingQun.setKeyWord(EncryptsUtil.decrypt(dingDingQun.getKeyWord()));
                }
            } catch (Exception e) {
                log.error("失败:{}",e);
            }
        }
    }
    @Override
    public Object selectAccepts(HashSet<Integer> userIds) {
        return null;
    }


    @Override
    public Object call() throws Exception {
        try{
            //1判断级别是否符合
            if(!outPut()){
                return null;
            }
            log.info("the alert information level is satisfied");

            //2:查询发送方
            selectFrom();
            log.info("perform select send dingdingqun finish");

            //3:拼接发送信息
            String sendMessage = dealMessage();
            log.info("perform deal message:{}", "*****");

            //4发送钉钉群消息
            sendMessage(sendMessage);
            log.info("dingdingqun message send finish");
            return null;
        }catch (Exception e){
            log.error("dingdingqun message send appear unknown error:{}",e);
            throw new Exception(e);
        }
    }

}
