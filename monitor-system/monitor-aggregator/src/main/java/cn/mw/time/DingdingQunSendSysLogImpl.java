package cn.mw.time;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.weixin.entity.DingdingqunFromEntity;
import cn.mw.monitor.weixin.service.SendMessageBase;
import cn.mw.monitor.weixin.service.impl.DingdingQunSendMessageiImpl;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 钉钉群机器人发送实现类
 */
public class DingdingQunSendSysLogImpl extends SendMessageBase {

    private static final Logger log = LoggerFactory.getLogger("DingdingQunSendSysLogImpl");

    //发送方（钉钉群机器人）
    private DingdingqunFromEntity qyEntity;
    private Map<String, Object> map;


    public DingdingQunSendSysLogImpl(Map<String, Object> map, HashSet<Integer> userIds, String ruleId) throws Exception {
        log.info("钉钉群map：" + map);
        this.map = map;
        this.userIds = userIds;
        this.ruleId = ruleId;
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
        }catch (Exception e){
            erroMessage = e.getMessage();
            log.info("error perform send message dingdingqun:{}",e.getMessage());
        }finally {
            /*saveHis("钉钉群",sendMessage,errcode,map.get("事件ID"),erroMessage,this.title,map.get("IP地址"),isAlarm, userIds);*/
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
        String result = "";
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
            log.error("钉钉群错误：" + e);
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
        StringBuffer content = new StringBuffer();
        content.append("资产名称:").append(map.get("hostName")).append(",").append('\n')
                .append("IP地址:").append(map.get("host")).append(",").append('\n')
                .append("级别:").append(map.get("severity_label")).append(",").append('\n')
                .append("类型:").append(map.get("facility_label")).append(",").append('\n')
                .append("时间:").append(map.get("@timestamp")).append(",").append('\n')
                .append("信息:").append(map.get("message")).append(",").append('\n')
                .append("数据源:").append(map.get("dataSourceName")).append(",").append('\n');

        return content.toString();
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
                log.error("钉钉群错误：" + e);
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

            //1:查询发送方
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
            log.info("dingdingqun message send appear unknown error:{}",e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

}
