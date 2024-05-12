package cn.mw.monitor.model.util;

import cn.mw.monitor.model.param.ConnectCheckModelEnum;
import cn.mw.monitor.model.param.MwModelMacrosValInfoParam;
import cn.mw.monitor.model.param.superfusion.MwLoginSuperFusionParam;
import cn.mw.monitor.model.proxy.param.*;
import cn.mw.monitor.model.service.MwModelVirtualizationService;
import cn.mw.monitor.model.service.impl.ProxySearch;
import cn.mw.monitor.model.type.MwModelMacrosValInfoParamType;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vijava.com.vmware.vim25.mo.ServiceInstance;

import javax.crypto.Cipher;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static cn.mw.monitor.model.service.impl.MwModelSuperFusionServiceImpl.HTTP_SUCCESS;

/**
 * @author qzg
 * @date 2023/2/19
 */
@Slf4j
@Service
public class ConnectCheckTest {
    private static String tokens;
    private static String tickets;
    private static String loginCookie = "LoginAuthCookie=";
    @Autowired
    private MwModelVirtualizationService mwModelVirtualizationService;

    @Autowired
    private ProxySearch proxySearch;

    public Reply connectCheck(List<MwModelMacrosValInfoParam> connectParam) {
        Reply reply = null;
        Integer modelId = 0;
        if (CollectionUtils.isNotEmpty(connectParam)) {
            modelId = connectParam.get(0).getModelId();
        }
        ConnectCheckModelEnum checkModelEnum = ConnectCheckModelEnum.valueOf(modelId);
        MwModelMacrosValInfoParam param = new MwModelMacrosValInfoParam();
        param.setModelId(modelId);

        String engineId = null;
        for (MwModelMacrosValInfoParam mwModelMacrosValInfoParam : connectParam) {
            if (MwModelMacrosValInfoParamType.EngineSel.getCode().equals(mwModelMacrosValInfoParam.getMacroType())) {
                engineId = mwModelMacrosValInfoParam.getMacroVal();
            }
        }

        log.info("do connectCheck local search");
        switch (checkModelEnum) {
            case VCENTER:
                //虚拟化连接校验
                reply = connectVcenter(connectParam, engineId);
                break;
            case CITRIXADC:
                //citrix连接校验
                reply = connectCitrixADC(connectParam, engineId);
                break;
            case RANCHER:
                //Rancher连接校验
                reply = connectRancher(connectParam, engineId);
                break;
            case HPCHASSIS:
                //带外资产HP刀片服务器连接校验
                reply = connectHPChassis(connectParam, engineId);
                Reply reply2 = connectHPChassis1(connectParam, engineId);
                break;
            case SUPERFUSION:
                //超融合连接校验
                reply = connectSuperFusion(connectParam, engineId);
                break;
            default:
        }

        return reply;
    }

    /**
     * VCenter API连接
     *
     * @param connectParam
     * @return
     */
    public Reply connectVcenter(List<MwModelMacrosValInfoParam> connectParam, String engineId) {

        List<ProxyInfo> proxyInfos = new ArrayList<>();
        VCenterCheckParam vCenterCheckParam = new VCenterCheckParam(connectParam);
        Boolean ret = proxySearch.doProxySearch(Boolean.class, proxyInfos, engineId, "mwVCenterService"
                , "connectVcenter", vCenterCheckParam, null);
        if (proxyInfos.size() > 0) {
            if (ret) {
                return Reply.ok("连接成功！");
            } else {
                return Reply.fail("连接失败！");
            }
        }

        ServiceInstance si = null;
        String url = "", userName = "", passWord = "";
        for (MwModelMacrosValInfoParam autoInfoParam : connectParam) {
            if (autoInfoParam.getMacro().equals("HOST")) {
                url = autoInfoParam.getMacroVal();
            }
            if (autoInfoParam.getMacro().equals("USERNAME")) {
                userName = autoInfoParam.getMacroVal();
            }
            if (autoInfoParam.getMacro().equals("PASSWORD")) {
                if ("1".equals(autoInfoParam.getMacroType())) {//type为1,，表示加密字段，需要解密
                    passWord = RSAUtils.decryptData(autoInfoParam.getMacroVal(), RSAUtils.RSA_PRIVATE_KEY);
                } else {
                    passWord = autoInfoParam.getMacroVal();
                }
            }
        }
        try {
            if (url.indexOf("/sdk") != -1) {
                si = new ServiceInstance(new URL(url), userName, passWord, true);
            } else {
                si = new ServiceInstance(new URL("https://" + url + "/sdk"), userName, passWord, true);
            }
        } catch (Exception e) {
            log.error("连接失败！",e);
            return Reply.fail("连接失败！");
        }
        return Reply.ok("连接成功！");
    }

    /**
     * Citrix ADC连接
     *
     * @param connectParam
     * @return
     */
    private Reply connectCitrixADC(List<MwModelMacrosValInfoParam> connectParam, String engineId) {

        List<ProxyInfo> proxyInfos = new ArrayList<>();
        CitrixADCCheckParam citrixADCCheckParam = new CitrixADCCheckParam(connectParam);
        Boolean ret = proxySearch.doProxySearch(Boolean.class, proxyInfos, engineId, "mwCitrixService"
                , "connectCitrixADC", citrixADCCheckParam, null);
        if (proxyInfos.size() > 0) {
            if (ret) {
                return Reply.ok("连接成功！");
            } else {
                return Reply.fail("连接失败！");
            }
        }

        String respContent = "";
        String url = "", userName = "", passWord = "", port = "";
        for (MwModelMacrosValInfoParam autoInfoParam : connectParam) {
            if (autoInfoParam.getMacro().equals("HOST")) {
                url = autoInfoParam.getMacroVal();
            }
            if (autoInfoParam.getMacro().equals("USERNAME")) {
                userName = autoInfoParam.getMacroVal();
            }
            if (autoInfoParam.getMacro().equals("PASSWORD")) {
                if ("1".equals(autoInfoParam.getMacroType())) {//type为1,，表示加密字段，需要解密
                    passWord = RSAUtils.decryptData(autoInfoParam.getMacroVal(), RSAUtils.RSA_PRIVATE_KEY);
                } else {
                    passWord = autoInfoParam.getMacroVal();
                }
            }
            if (autoInfoParam.getMacro().equals("PORT")) {
                port = autoInfoParam.getMacroVal();
            }
        }
        try {
            HttpGet httpGet = new HttpGet("http://" + url + ":" + port + "/nitro/v1/config/login");
            log.info("http地址001：" + httpGet);
            httpGet.addHeader("X-NITRO-USER", userName);
            httpGet.addHeader("X-NITRO-PASS", passWord);
            CloseableHttpClient client = HttpClients.createDefault();
            HttpResponse resp = client.execute(httpGet);//执行时机
            if (resp.getStatusLine().getStatusCode() == 200) {
                HttpEntity he = resp.getEntity();
                respContent = EntityUtils.toString(he, "UTF-8");
            }
            //再次判断请求，修改api路径
            if (respContent.length() <= 60) {
                httpGet = new HttpGet("http://" + url + ":" + port + "/nitro/v1/stat/login");
                log.info("http地址002：" + httpGet);
                httpGet.addHeader("X-NITRO-USER", userName);
                httpGet.addHeader("X-NITRO-PASS", passWord);
                client = HttpClients.createDefault();
                resp = client.execute(httpGet);//执行时机
                if (resp.getStatusLine().getStatusCode() == 200) {
                    HttpEntity he = resp.getEntity();
                    respContent = EntityUtils.toString(he, "UTF-8");
                }
            }
        } catch (Throwable e) {
            log.error("fail to getCitrixDataInfo case:{}", e);
            return Reply.fail("连接失败");
        }
        return Reply.ok("连接成功");
    }

    /**
     * Rancher连接
     *
     * @param connectParam
     * @return
     */
    private Reply connectRancher(List<MwModelMacrosValInfoParam> connectParam, String engineId) {

        List<ProxyInfo> proxyInfos = new ArrayList<>();
        RancherCheckParam rancherCheckParam = new RancherCheckParam(connectParam);
        Boolean ret = proxySearch.doProxySearch(Boolean.class, proxyInfos, engineId, "mwRancherService"
                , "connectRancher", rancherCheckParam, null);
        if (proxyInfos.size() > 0) {
            if (ret) {
                return Reply.ok("连接成功！");
            } else {
                return Reply.fail("连接失败！");
            }
        }
        String respContent = "";
        String https = "https://";
        String rancherV3 = "/v3/";
        String ip = "", token = "";
        for (MwModelMacrosValInfoParam autoInfoParam : connectParam) {
            if (autoInfoParam.getMacro().equals("HOST")) {
                ip = autoInfoParam.getMacroVal();
            }
            if (autoInfoParam.getMacro().equals("TOKENS")) {
                if ("1".equals(autoInfoParam.getMacroType()) && autoInfoParam.getMacroVal().length() > 128) {//type为1,，表示加密字段，需要解密
                    token = RSAUtils.decryptData(autoInfoParam.getMacroVal(), RSAUtils.RSA_PRIVATE_KEY);
                } else {
                    token = autoInfoParam.getMacroVal();
                }
            }
        }
        InputStream is;
        String url = https + ip + rancherV3;
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            URLConnection connection = new URL(url).openConnection();
            HttpsURLConnection httpConn = (HttpsURLConnection) connection;
            httpConn.setRequestProperty("Authorization", "Bearer " + token);
            is = httpConn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            is.close();
            if (rd != null) {
                return Reply.ok("连接成功");
            }
            return Reply.fail("连接失败");
        } catch (Exception ex) {
            log.error("连接失败", ex);
            return Reply.fail("连接失败");
        }
    }

    /**
     * 带外资产HP刀片服务器连接
     *
     * @param connectParam
     * @return
     */
    private Reply connectHPChassis(List<MwModelMacrosValInfoParam> connectParam, String engineId) {
        try {
            log.info("刀箱连接测试方法1");
            List<ProxyInfo> proxyInfos = new ArrayList<>();
            HPChassisCheckParam hpChassisCheckParam = new HPChassisCheckParam(connectParam);
            Boolean ret = proxySearch.doProxySearch(Boolean.class, proxyInfos, engineId, "mwHPChassisService"
                    , "connectHPChassis", hpChassisCheckParam, null);
            if (proxyInfos.size() > 0) {
                if (ret) {
                    return Reply.ok("连接成功！");
                } else {
                    return Reply.fail("连接失败！");
                }
            }
            String respContent = "";
            String IP = "", userName = "", passWord = "";
            for (MwModelMacrosValInfoParam autoInfoParam : connectParam) {
                if (autoInfoParam.getMacro().equals("HOST")) {
                    IP = autoInfoParam.getMacroVal();
                }
                if (autoInfoParam.getMacro().equals("USERNAME")) {
                    userName = autoInfoParam.getMacroVal();
                }
                if (autoInfoParam.getMacro().equals("PASSWORD")) {
                    if ("1".equals(autoInfoParam.getMacroType())) {//type为1,，表示加密字段，需要解密
                        passWord = RSAUtils.decryptData(autoInfoParam.getMacroVal(), RSAUtils.RSA_PRIVATE_KEY);
                    } else {
                        passWord = autoInfoParam.getMacroVal();
                    }
                }
            }
            // 创建HttpClient对象
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // 设置iLO地址和认证信息
            String baseUrl = "https://" + IP;
            // 获取刀箱信息
            String iloUrl = baseUrl + "/redfish/v1";
            String iloUsername = userName;
            String iloPassword = passWord;
            log.info("conncetTextIOL::connectParam" + connectParam);
            // 创建CredentialsProvider对象并设置用户名和密码
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(iloUsername, iloPassword));

            // 创建HttpPost对象，设置认证头和请求体
            HttpPost httpPost = new HttpPost(iloUrl + "/SessionService/Sessions");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((iloUsername + ":" + iloPassword).getBytes()));
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // 执行请求并获取响应
            HttpResponse response = null;
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            // 解析响应，获取访问令牌
            String responseString = EntityUtils.toString(entity);
            // 解析responseString，获取access token
            log.info("responseString::" + responseString);

            if (Strings.isNullOrEmpty(responseString)) {
                return Reply.fail("连接失败");
            } else {
                return Reply.ok("连接成功");
            }
        } catch (Exception e) {
            return Reply.fail("连接失败");
        }

    }

    /**
     * 带外资产HP刀片服务器连接
     *
     * @param connectParam
     * @return
     */
    private Reply connectHPChassis1(List<MwModelMacrosValInfoParam> connectParam, String engineId) {
        try {
            log.info("刀箱连接测试方法2");
            List<ProxyInfo> proxyInfos = new ArrayList<>();
            HPChassisCheckParam hpChassisCheckParam = new HPChassisCheckParam(connectParam);
            Boolean ret = proxySearch.doProxySearch(Boolean.class, proxyInfos, engineId, "mwHPChassisService"
                    , "connectHPChassis", hpChassisCheckParam, null);
            if (proxyInfos.size() > 0) {
                if (ret) {
                    return Reply.ok("连接成功！");
                } else {
                    return Reply.fail("连接失败！");
                }
            }
            String respContent = "";
            String IP = "", userName = "", passWord = "";
            for (MwModelMacrosValInfoParam autoInfoParam : connectParam) {
                if (autoInfoParam.getMacro().equals("HOST")) {
                    IP = autoInfoParam.getMacroVal();
                }
                if (autoInfoParam.getMacro().equals("USERNAME")) {
                    userName = autoInfoParam.getMacroVal();
                }
                if (autoInfoParam.getMacro().equals("PASSWORD")) {
                    if ("1".equals(autoInfoParam.getMacroType())) {//type为1,，表示加密字段，需要解密
                        passWord = RSAUtils.decryptData(autoInfoParam.getMacroVal(), RSAUtils.RSA_PRIVATE_KEY);
                    } else {
                        passWord = autoInfoParam.getMacroVal();
                    }
                }
            }

            String baseUrl = "https://" + IP;
            String authentication = userName + ":" + passWord;
            String authorization = "Basic " + Base64.getEncoder().encodeToString(authentication.getBytes());
            // 获取刀箱信息
            String chassisUrl = baseUrl + "/redfish/v1/Chassis";
            URL url = new URL(chassisUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", authorization);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                respContent = response.toString();
            } else {
                log.info("Failed to retrieve data from iLO4. Response code: " + responseCode);
            }
            log.info("Chassis Info::" + respContent);
            conn.disconnect();
            if (Strings.isNullOrEmpty(respContent)) {
                return Reply.fail("连接失败");
            } else {
                return Reply.ok("连接成功");
            }
        } catch (Exception e) {
            return Reply.fail("连接失败");
        }
    }


    /**
     * 超融合连接
     *
     * @param connectParam
     * @return
     */
    private Reply connectSuperFusion(List<MwModelMacrosValInfoParam> connectParam, String engineId) {

        List<ProxyInfo> proxyInfos = new ArrayList<>();
        try {
            SuperFusionCheckParam superFusionCheckParam = new SuperFusionCheckParam(connectParam);

            String IP = "", userName = "", passWord = "";
            for (MwModelMacrosValInfoParam autoInfoParam : connectParam) {
                if (autoInfoParam.getMacro().equals("HOST")) {
                    IP = autoInfoParam.getMacroVal();
                }
                if (autoInfoParam.getMacro().equals("USERNAME")) {
                    userName = autoInfoParam.getMacroVal();
                }
                if (autoInfoParam.getMacro().equals("PASSWORD")) {
                    if ("1".equals(autoInfoParam.getMacroType())) {//type为1,，表示加密字段，需要解密
                        passWord = RSAUtils.decryptData(autoInfoParam.getMacroVal(), RSAUtils.RSA_PRIVATE_KEY);
                    } else {
                        passWord = autoInfoParam.getMacroVal();
                    }
                }
            }
            MwLoginSuperFusionParam param = new MwLoginSuperFusionParam();
            param.setUserName(userName);
            param.setUrl(IP);
            //密码加密，生成指定密文
            String encryptStr = encryptedPassWd(passWord, IP);
            param.setPassword(encryptStr);
            log.info("获取的超融合登录参数::" + param);
            //获取token、ticket
            getTicker(param);
            if (!Strings.isNullOrEmpty(tokens) && !Strings.isNullOrEmpty(tickets)) {
                return Reply.ok("登录成功");
            }
            return Reply.fail("连接失败");
        } catch (Exception ex) {
            return Reply.fail("连接失败");
        }
    }

    /**
     * 使用公钥进行加密
     *
     * @param plaintext
     * @return
     */
    private String encryptedPassWd(String plaintext, String superFusionIp) {
        String encryptStr = "";
        //获取公钥字符串
        String publicKeyUrl = "https://" + superFusionIp + "/vapi/json/public_key";
        String jsonText = ModelOKHttpUtils.builder().url(publicKeyUrl)
                .get()
                .sync();
        JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
        log.info("get publicKeyUrl"+strInfoJson);
        String publicKeyStr = "";
        //接口返回成功
        if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
            publicKeyStr = (String) strInfoJson.get("data");
        }
        try {
            log.info("get PublicKey::" + publicKeyStr);
            // 将Base64编码表示的公钥转换为公钥对象
            PublicKey publicKey = getPublicKeyFromBase64(publicKeyStr);
            log.info("get publicKey::" + publicKey);
            // 使用公钥进行加密
            byte[] encryptedData = encryptRSA(plaintext, publicKey);
            // 将加密后的数据转换为十六进制字符串并输出
            encryptStr = bytesToHex(encryptedData);
            log.info("加密后的数据：" + encryptStr);
        } catch (Exception e) {
            log.error("使用RSA公钥字符串加密失败", e);
        }
        return encryptStr;
    }

    /**
     * 获取token
     */
    public void getTicker(MwLoginSuperFusionParam param) {
        String username = param.getUserName();
        String password = param.getPassword();
        String ip = param.getUrl();
        String url = "https://" + ip + "/vapi/extjs/access/ticket";
        String data = "";
        try {
            data = ModelOKHttpUtils.builder().url(url)
                    // 有参数的话添加参数，可多个
                    .addParam("username", username)
                    .addParam("password", password)
                    // 也可以添加多个
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    //isJsonPost true等于json的方式提交数据，类似postman里post方法的raw,false等于普通的表单提交
                    //isDefaultMediaType true使用application/json,false使用application/x-www-form-urlencoded
                    .post(false, false)
                    .sync();

            JSONObject strInfoJson = JSONObject.parseObject(data != null ? data : "");
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                Map map = (Map) strInfoJson.get("data");
                if (map != null) {
                    if (map.get("CSRFPreventionToken") != null) {
                        tokens = map.get("CSRFPreventionToken").toString();
                    }
                    if (map.get("ticket") != null) {
                        tickets = map.get("ticket").toString();
                    }
                }
            }
            log.info("get tokens info::" + tokens + ";tickets:" + tickets);
        } catch (Exception e) {
            log.error("获取token接口失败", e);
        }
    }

    // 将Base64编码表示的公钥转换为公钥对象
    public static PublicKey getPublicKeyFromBase64(String publicKeyBase64) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        BigInteger exp = new BigInteger("10001",16);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(publicKeyBase64, 16), exp);
        return keyFactory.generatePublic(spec);
    }

    // 使用公钥进行加密
    public static byte[] encryptRSA(String plaintext, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // 添加算法和填充方式
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    // 将字节数组转换为十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}
