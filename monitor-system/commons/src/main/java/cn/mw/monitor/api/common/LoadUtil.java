package cn.mw.monitor.api.common;

import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Map;

import static org.apache.http.client.config.RequestConfig.*;

/**
 * @author bkc
 * @create 2020-06-30 23:55
 */
@Slf4j
public class LoadUtil {

    /**
     * 向指定的地址发送post请求
     */
    public static String post(String url, String data,AlertRuleTableCommons alertRuleTable) {
        try {
            Proxy proxy = null;
            if(alertRuleTable!=null && alertRuleTable.getProxyState() != null){
                proxy = getProxy(alertRuleTable);
            }
            URL urlObj = new URL(url);
            log.info("url:" + url);
            HttpURLConnection connection = null;
            if(proxy != null){
                connection = (HttpsURLConnection)urlObj.openConnection(proxy);
            }else{
                connection = (HttpsURLConnection)urlObj.openConnection();
            }
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");

            // 要发送数据出去，必须要设置为可发送数据状态
            connection.setDoOutput(true);
            connection.setDoInput(true);
            // 获取输出流
            OutputStream os = connection.getOutputStream();
            // 写出数据
            os.write(data.getBytes("UTF-8"));
            os.flush();
            // 获取输入流
            InputStream is = connection.getInputStream();
            byte[] b = new byte[1024];
            int len;
            StringBuilder sb = new StringBuilder();
            while ((len = is.read(b)) != -1) {
                sb.append(new String(b, 0, len));
            }
            os.close();
            is.close();
            return sb.toString();
        } catch (Exception e) {
            log.error("错误返回 :{}" + alertRuleTable + "==" + e);
        }
        return null;
    }

    public static String post2(String url, String data) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)urlObj.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");

            // 要发送数据出去，必须要设置为可发送数据状态
            connection.setDoOutput(true);
            connection.setDoInput(true);
            // 获取输出流
            OutputStream os = connection.getOutputStream();
            // 写出数据
            os.write(data.getBytes("UTF-8"));
            os.flush();
            // 获取输入流
            InputStream is = connection.getInputStream();
            byte[] b = new byte[1024];
            int len;
            StringBuilder sb = new StringBuilder();
            while ((len = is.read(b)) != -1) {
                sb.append(new String(b, 0, len));
            }

            os.close();
            is.close();
            return sb.toString();
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
        }finally {

        }
        return null;
    }


    public static String get(String urlstr) {
        return get(urlstr, null);
    }
    /**
     * 向指定的地址发送get请求
     *
     * @param urlstr
     */
    public static String get(String urlstr, AlertRuleTableCommons alertRuleTable) {
        log.info("代理ip alertRuleTable" + alertRuleTable);
        StringBuffer buffer = new StringBuffer();
        try {
            Proxy proxy = null;
            if(alertRuleTable!=null && alertRuleTable.getProxyState() != null){
                proxy = getProxy(alertRuleTable);
            }
            URL url = new URL(urlstr);
            HttpURLConnection httpUrlConn = null;
            log.info("代理ip proxy" + proxy);
            if(proxy != null){
                httpUrlConn = (HttpURLConnection) url.openConnection(proxy);
            }else{
                httpUrlConn = (HttpURLConnection) url.openConnection();
            }
            httpUrlConn.setDoOutput(false);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);

            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.connect();

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            httpUrlConn.disconnect();
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
        }
        return buffer.toString();
    }

    public static Proxy getProxy(AlertRuleTableCommons alertRuleTable){
        if(alertRuleTable.getProxyState()){
            //设置认证信息
            System.setProperty("http.proxyUser", alertRuleTable.getProxyAccount());
            System.setProperty("http.proxyPassword", alertRuleTable.getProxyPassword());
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
            System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
            Authenticator.setDefault(
                    new Authenticator() {
                        @Override
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(alertRuleTable.getProxyAccount(), alertRuleTable.getProxyPassword().toCharArray());
                        }
                    }
            );
            //构造proxy的地址和端口并返回
            SocketAddress socketAddress = new InetSocketAddress(alertRuleTable.getProxyIp(), Integer.parseInt(alertRuleTable.getProxyPort()));
            Proxy proxy = new Proxy(Proxy.Type.HTTP,socketAddress);
            return proxy;
        }
        return null;
    }

    public static String httpPost(String url, Map<String, Object> mapParam,Map<String, String> headParam){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            if(mapParam != null){
                for(Map.Entry<String, Object> entry: mapParam.entrySet()) {
                    builder.addPart(entry.getKey(),new StringBody( entry.getValue().toString(), ContentType.create("text/plain", Consts.UTF_8)));
                }
            }
            if (headParam != null) {
                for (Map.Entry<String, String> entry : headParam.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e){
            log.error("错误返回 :{}",e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("错误返回 :{}",e);
            }
        }
        return null;
    }
    public static String doPost(String url, Map<String, Object> param, Map<String, String> header, AlertRuleTableCommons alertRuleTable) throws IOException {
        // 创建Httpclient对象
        HttpClient httpclient;
        if(alertRuleTable != null && alertRuleTable.getProxyState()){
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(alertRuleTable.getProxyIp(), Integer.parseInt(alertRuleTable.getProxyPort())),
                    new UsernamePasswordCredentials(alertRuleTable.getProxyAccount(), alertRuleTable.getProxyPassword()));
            httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider)
                    .setProxy(new HttpHost(alertRuleTable.getProxyIp(), Integer.parseInt(alertRuleTable.getProxyPort())))
                    .setDefaultRequestConfig(custom().setProxyPreferredAuthSchemes(Collections.singleton(AuthSchemes.BASIC)).build())
                    .build();
        }else {
            httpclient = HttpClients.createDefault();
        }

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

    public static String sendPostRequest(String url, String requestBody, Map<String, String> header, AlertRuleTableCommons alertRuleTable) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpclient;
        if(alertRuleTable != null && alertRuleTable.getProxyState()){
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(alertRuleTable.getProxyIp(), Integer.parseInt(alertRuleTable.getProxyPort())),
                    new UsernamePasswordCredentials(alertRuleTable.getProxyAccount(), alertRuleTable.getProxyPassword()));
            httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider)
                    .setProxy(new HttpHost(alertRuleTable.getProxyIp(), Integer.parseInt(alertRuleTable.getProxyPort())))
                    .setDefaultRequestConfig(custom().setProxyPreferredAuthSchemes(Collections.singleton(AuthSchemes.BASIC)).build())
                    .build();
        }else {
            httpclient = HttpClients.createDefault();
        }
        HttpPost httpPost = new HttpPost(url);
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
        }
        StringEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity);
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }


}
