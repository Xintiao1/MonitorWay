package cn.mw.monitor.activiti.util;

import cn.mw.monitor.activiti.entiy.OA.NotifyTodoRemoveContext;
import cn.mw.monitor.activiti.entiy.OA.NotifyTodoSendContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.security.cert.X509Certificate;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author lumingming
 * @createTime 02 15:23
 * @description
 */
public class TestOA {


    private TestOA(){}
    private  static  final  TestOA single = new TestOA();

    public static  TestOA getInstance(){
        return single;
    }

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/activiti/service/impl/ActivitiServiceImpl");
    public Boolean sendTodo(String url, String admin, String useName, String loginName, NotifyTodoSendContext notifyTodoSendContext) throws IllegalAccessException {

        url = url+"/sys-notify/sysNotifyTodoRestService/sendTodo";
        HttpHeaders headers = new HttpHeaders();
        addAuth(headers,admin+":"+useName);
        Map<String,Object> paramBody =getObjectToMap(notifyTodoSendContext);
        paramBody.put("targets", "{\"LoginName\":\""+loginName+"\"}");
        SendMessger(headers,paramBody,url);
        return true;
    }


    public Boolean deleteTodo(String url, String admin, String useName, String loginName, NotifyTodoRemoveContext notifyTodoRemoveContext) throws IllegalAccessException {

        url = url+"/sys-notify/sysNotifyTodoRestService/deleteTodo";
        HttpHeaders headers = new HttpHeaders();
        addAuth(headers,admin+":"+useName);
        Map<String,Object> paramBody =getObjectToMap(notifyTodoRemoveContext);
        paramBody.put("targets", "{\"LoginName\":\""+loginName+"\"}");
        SendMessger(headers,paramBody,url);
        return true;
    }

    public Boolean setTodoDone(String url, String admin, String useName, String loginName, NotifyTodoRemoveContext notifyTodoRemoveContext) throws IllegalAccessException {
        url = url+"/sys-notify/sysNotifyTodoRestService/setTodoDone";
        HttpHeaders headers = new HttpHeaders();
        addAuth(headers,admin+":"+useName);
        Map<String,Object> paramBody =getObjectToMap(notifyTodoRemoveContext);
        paramBody.put("targets", "{\"LoginName\":\""+loginName+"\"}");
        SendMessger(headers,paramBody,url);
        return true;
    }


    private void SendMessger(HttpHeaders headers, Map<String, Object> paramBody, String url) {
        logger.info("发送参数查看"+paramBody);
        logger.info("发送地址查看"+url);
        // 定义http请求实体对象
        HttpEntity<Map<String,Object>> entity = new HttpEntity<Map<String,Object>>(paramBody,headers);

        // 发送请求
        RestTemplate template = createRestTemplate();

        try{
            ResponseEntity<Map> exchange = template.exchange(url, HttpMethod.POST, entity, Map.class);
            logger.info("返回结果查看"+exchange.getBody());
        }catch (Exception e){
            logger.info("报错信息："+e.toString());
        }


    }

    private static RestTemplate createRestTemplate() {
        // 禁用SSL证书验证
        CloseableHttpClient httpClient = disableSslVerification();

        // 创建使用Apache HttpClient的ClientHttpRequestFactory
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 创建RestTemplate并设置ClientHttpRequestFactory
        return new RestTemplate(requestFactory);
    }

    private static CloseableHttpClient disableSslVerification() {
        try {
            // 创建一个信任所有证书的SSL上下文
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();

            // 创建HttpClient并设置信任所有证书的SSL上下文
            return HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to disable SSL verification", e);
        }
    }

    public static void main(String[] args) {
        // 定义请求接口URL
        String url = "http://127.0.0.1:8080/ekp/api/sys-notify/sysNotifyTodoRestService/getTodo";

        // 定义header对象
        HttpHeaders headers = new HttpHeaders();

        // 如果EKP对该接口启用了Basic认证，那么客户端需要加入认证header信息
        String accountID = "admin"; // 认证用户名
        String accountPassword = "123456"; // 认证密码
        addAuth(headers,accountID+":"+accountPassword);

        // 定义请求参数Map
        Map<String,Object> paramBody = new HashMap<String,Object>();
        paramBody.put("targets", "{\"LoginName\":\"admin\"}");
        paramBody.put("type", 0);


    }

    private static void addAuth(HttpHeaders headers,String yourEncryptedWorlds){
        byte[] encodedAuth = org.springframework.security.crypto.codec.Base64.encode(yourEncryptedWorlds.getBytes(Charset.forName("UTF-8")));
        String authHeader = "Basic " + new String( encodedAuth );
        headers.set("Authorization", authHeader );
    }


    public Map<String, Object> getObjectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Class<?> clazz = obj.getClass();
        //System.out.println(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);
            if (value == null) {
                value = "";
            }
            map.put(fieldName, value);
        }
        return map;
    }
}
