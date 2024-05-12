package cn.mw.monitor.weixin.util;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author lumingming
 * @createTime 20210607 14:42
 * @description 07
 */

@Component
@Slf4j
public class SendUnifiedInterFace implements InitializingBean {

    @Value("${lvmeng.authKey}")
    private String authKey;
    @Value("${lvmeng.url}")
    private String url;
    private static final Pattern IPV4_REGEX =
            Pattern.compile(
                    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static final Pattern IPV6_REGEX =
            Pattern.compile(
                    "^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$");



    private  String doPost(String url, String content) throws IOException {

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = getHttps().newCall(request).execute();
        String string = response.body().string();

        return string;
    }




    public   Map<String,Object> sreachIndex(String ip) throws IOException {
        Map<String,Object> res = new HashMap<>();
        String content = "target=divertManual&action_type=load&auth_key="+authKey;
        String result = doPost(url,content);
//        String result = "{\n" +
//                "    \"data\": [\n" +
//                "        {\n" +
//                "            \"ip\": \"222.16.42.161\",\n" +
//                "            \"mask\": \"255.255.255.255\",\n" +
//                "            \"dst\": \"192.168.10.9\",\n" +
//                "            \"daemon\": \"IPv4/\",\n" +
//                "            \"status\": \"2\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"华工可断网测试网站http://eonl... \\r\\\\\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"185.198.189.21\",\n" +
//                "            \"mask\": \"255.255.255.255\",\n" +
//                "            \"dst\": \"192.168.10.9\",\n" +
//                "            \"daemon\": \"IPv4/\",\n" +
//                "            \"status\": \"1\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"网盾恶意IP封堵 \\r\\\\\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"198.144.149.131\",\n" +
//                "            \"mask\": \"255.255.255.255\",\n" +
//                "            \"dst\": \"192.168.10.9\",\n" +
//                "            \"daemon\": \"IPv4/\",\n" +
//                "            \"status\": \"1\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"垃圾邮件、恶意软件 \\r\\\\\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"210.38.1.148\",\n" +
//                "            \"mask\": \"255.255.255.255\",\n" +
//                "            \"dst\": \"192.168.10.9\",\n" +
//                "            \"daemon\": \"IPv4/\",\n" +
//                "            \"status\": \"1\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"远江盛邦管控地址 \\r\\\\\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"86.106.181.102\",\n" +
//                "            \"mask\": \"255.255.255.255\",\n" +
//                "            \"dst\": \"192.168.10.9\",\n" +
//                "            \"daemon\": \"IPv4/\",\n" +
//                "            \"status\": \"1\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"存在存在对外提供后门脚本下载，并通相关情报系统对比，属于恶意IP\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"138.201.162.105\",\n" +
//                "            \"mask\": \"255.255.255.255\",\n" +
//                "            \"dst\": \"192.168.10.9\",\n" +
//                "            \"daemon\": \"IPv4/\",\n" +
//                "            \"status\": \"1\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"存在存在对外提供后门脚本下载，并通相关情报系统对比，属于恶意IP\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"202.38.213.91\",\n" +
//                "            \"mask\": \"255.255.255.255\",\n" +
//                "            \"dst\": \"192.168.10.9\",\n" +
//                "            \"daemon\": \"IPv4/\",\n" +
//                "            \"status\": \"1\",\n" +
//                "            \"extend\": \"1\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"2001:250:3000:4be4:e5e5:7267:27be:2dd0\",\n" +
//                "            \"mask\": \"128\",\n" +
//                "            \"dst\": \"2001:DA8:A2:1609::1609\",\n" +
//                "            \"daemon\": \"IPv6/\",\n" +
//                "            \"status\": \"2\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"test\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"2001:da8:203c:2::155\",\n" +
//                "            \"mask\": \"128\",\n" +
//                "            \"dst\": \"2001:DA8:A2:1609::1609\",\n" +
//                "            \"daemon\": \"IPv6/\",\n" +
//                "            \"status\": \"2\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"test\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"ip\": \"56.23.52.41\",\n" +
//                "            \"mask\": \"255.255.255.255\",\n" +
//                "            \"dst\": \"192.168.10.9\",\n" +
//                "            \"daemon\": \"IPv4/\",\n" +
//                "            \"status\": \"2\",\n" +
//                "            \"extend\": \"1\",\n" +
//                "            \"description\": \"测试断网\"\n" +
//                "        }\n" +
//                "    ],\n" +
//                "    \"hash\": \"a667a8968a910c7c925247474da74397\"\n" +
//                "}";

        Map<String,Object> map = jsonMap(result);
        List<Map<String,String>> ipConfig = (List<Map<String, String>>) map.get("data");

        Map<String,Object> select = new HashMap<>();
        List<Map<String,String>> killIp = new ArrayList<>();
        List<String> IpFour = new ArrayList<>();
        List<String> IpSix =new ArrayList<>();
        for (Integer i = 0; i < ipConfig.size(); i++) {
            Map<String,String> mapIp = ipConfig.get(i);
            if (ip==null){
                Map<String,Object> selectValue = new HashMap<>();
//              if (mapIp.get("status").toString().equals("1")){
                mapIp.put("index",i.toString());
                killIp.add(mapIp);
//              }
                if (select.get(mapIp.get("daemon"))==null){
                    selectValue.put("daemon",mapIp.get("daemon"));
                    selectValue.put("dst",mapIp.get("dst"));
                    selectValue.put("mask",mapIp.get("mask"));
                    selectValue.put("description",mapIp.get("description"));
                    select.put(mapIp.get("daemon"),selectValue);
                }

                if (isIPv4Address(mapIp.get("ip"))){
                    IpFour.add(mapIp.get("ip"));
                }else {
                    IpSix.add(mapIp.get("ip"));
                }
            }else {
                if (mapIp.get("ip").contains(ip)){
                    res.put("haveIP",true);
                    res.put("index",i);
                    return res;
                }
            }

        }
        Collections.reverse(killIp);
        res.put("select",select);
        res.put("killIp", killIp);
        res.put("IpFour",IpFour);
        res.put("IpSix",IpSix);
        res.put("hash",map.get("hash").toString());
        return res;
    }


    public  Map<String,String> sreachhash() throws IOException {
        Map<String,String> res = new HashMap<>();
        String content = "auth_key="+authKey+"&target=divertManual&action_type=gethash";

        String result = doPost(url,content);

        Map<String,Object> map = jsonMap(result);

        res.put("hash",map.get("hash").toString());
        log.info("绿盟has接口发送:"+result);
        return res;
    }

    public  Map<String,String> addIndex(String ip,String daemon,String mask,String dst,String description) throws IOException {
        Map<String,String> mapHas = sreachhash();
        String hash =mapHas.get("hash");
        Map<String,Object> reslut= sreachIndex(ip);

        Map<String,String> res = new HashMap<>();
        if (reslut.get("haveIP")!=null&&reslut.get("haveIP").toString().equals("true")){
            res = enable(Integer.parseInt(reslut.get("index").toString()));
        }
        else {
            String content = "target=divertManual&action_type=add&auth_key="+authKey+"&hash="+hash+"&configs=";
            String config ="{\"ip\":\""+ip+"\",\"mask\":\""+mask+"\",\"dst\":\""+dst+"\",\"daemon\":\""+daemon+"\", \"status\": \"1\",\"extend\": \"1\",\"description\": \""+description+"\"}";
            content = content+config;

            String result = doPost(url,content);
            Map<String,Object> map = jsonMap(result);
            log.info("绿盟新增接口发送:"+map);
            if (map.get("result").toString().equals("success")){
                res.put("result",map.get("result").toString());
            }else {
                res.put("result",map.get("result").toString());
                res.put("content",map.get("content").toString());
            }
        }
//        Map<String,Object> map = jsonMap("{\"result\":\"success\",\"content\":{\"actioninfos\":[\"\"]}}");
//        if (map.get("result").toString().equals("ssuccess")){
//            res.put("result",map.get("result").toString());
//        }else {
//            res.put("result",map.get("result").toString());
//            res.put("content",map.get("content").toString());
//        }
        return res;
    }

    public  Map<String,String> enable(Integer index) throws IOException {
        Map<String,Object> mapHas = sreachIndex(null);
        String hash =mapHas.get("hash").toString();
        Map<String,String> res = new HashMap<>();
        String content = "auth_key="+authKey+"&target=divertManual&action_type=enable&hash="+hash+"&check_route=0&index="+index;
        String result = doPost(url,content);
        Map<String,Object> map = jsonMap(result);
        if (map.get("result").toString().equals("success")){
            res.put("result",map.get("result").toString());
        }else {
            res.put("result",map.get("result").toString());
            res.put("content",map.get("content").toString());
        }
        log.info("绿盟回复接口发送:"+res);
        return res;
    }

    public  Map<String,String> disenable(String index) throws IOException {
        Map<String,Object> mapHas = sreachIndex(null);
        String hash =mapHas.get("hash").toString();
        Map<String,String> res = new HashMap<>();
        String content = "auth_key="+authKey+"&target=divertManual&action_type=disable&hash="+hash+"&index="+index;
        String result = doPost(url,content);
        Map<String,Object> map = jsonMap(result);
        ////System.out.println(map.toString());
        if (map.get("result").toString().equals("ssuccess")){
            res.put("result",map.get("result").toString());
        }else {
            res.put("result",map.get("result").toString());
            res.put("content",map.get("content").toString());
        }
        return res;
    }






//    public static void main(String[] args) {
//        String content = "auth_key="+1+"&target=divertManual&action_type=add&hash="+2+"configs=";
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("ip",1);
//        jsonObject.put("mask",1);
//        jsonObject.put("dst",1);
//        jsonObject.put("daemon",1);
//        jsonObject.put("description",1);
//        jsonObject.put("extend","1");
//        jsonObject.put("status","1");
//        content =content+jsonObject.toJSONString();
//        ////System.out.println(content);
//    }






    public  static  Map<String,Object> jsonMap(String result){
        Map<String,Object> res = null;
        try {
            Gson gson = new Gson();
            res = gson.fromJson(result,new TypeToken<Map<String,Object>>(){}.getType());
        }catch (Exception e){
            log.info("绿盟接口:"+e);
        }
        return res;
    }

    public  static List<Map<String,String>> jsonList(String result){
        List<Map<String,String>> objectList = new ArrayList<>();
        try {
            Gson gson = new Gson();
            objectList.addAll(gson.fromJson(result,new TypeToken<List<Map<String,String>>>(){}.getType()));
        }catch (Exception e){
            log.info("绿盟接口:"+e);
        }
        return objectList;
    }

    protected static SSLSocketFactory createTrustAllSSLFactory(TrustAllManager trustAllManager) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{trustAllManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return ssfFactory;
    }



    //创建信任所有证书的OkHttpClient
    public static OkHttpClient getHttps() {


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
//            .sslSocketFactory(SSLSocketClient.getSSLSocketFactory())
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .build();


        return okHttpClient;
    }



    //这里是创建一个SSLSocketFactory,提供给上面的 .sslSocketFactory()
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    public  boolean isIPv4Address(String input){
        return IPV4_REGEX.matcher(input).matches();
    }

    public    boolean isIPv6Address(String input){
        return IPV6_REGEX.matcher(input).matches();
    }

    public  boolean IsIPAddress(String input,String daemon){
        Boolean ipv4 = isIPv4Address(input);
        Boolean ipv6 = isIPv6Address(input);
        if (!ipv4&&!ipv6){
            return false;
        }else {
            if (daemon.contains("IPv4")&&ipv4){
                return true;
            }
            else if (daemon.contains("IPv6")&&ipv6){
                return true;
            }else if (!daemon.contains("IPv4")&&!daemon.contains("IPv6")){
                return true;
            }else {
                return false;
            }
        }
    }




    @Override
    public void afterPropertiesSet() throws Exception {

    }
}