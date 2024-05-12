package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.param.ilosystem.ILOInstanceParam;
import cn.mw.monitor.model.service.MwModelILOSystemService;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.monitor.util.TransferUtils;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 刀片服务器Service
 *
 * @author qzg
 * @date 2023/4/17
 */
@Service
@Slf4j
public class MwModelILOSystemServiceImpl implements MwModelILOSystemService {

    @Value("${model.instance.batchFetchNum}")
    private int insBatchFetchNum;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private ProxySearch proxySearch;

    @Override
    public Reply getAllILODataInfo(ILOInstanceParam param) {
        try {
            QueryModelInstanceParam qParam = new QueryModelInstanceParam();
            TransferUtils.transferBean(param, qParam);
            //根据实例获取es数据信息
            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(qParam);
            //获取连接信息，URL、用户名、密码
            String userName = "";
            String ip = "";
            String password = "";
            String assetsId = "";
            String monitorServerName = "";
            Integer monitorServerId = 0;
            Integer vcenterModelId = null;
            for (Map<String, Object> m : listInfo) {
                userName = m.get("USERNAME").toString();
                ip = m.get("HOST").toString();
                password = RSAUtils.decryptData(m.get("PASSWORD") != null ? m.get("PASSWORD").toString() : "", RSAUtils.RSA_PRIVATE_KEY);
            }
            param.setIPAdress(ip);
            param.setUserName(userName);
            param.setPassWord(password);

            conncetTextIOL(param);


            String baseUrl = "https://" + ip;
            // 获取刀箱信息
            String chassisUrl = baseUrl + "/redfish/v1/Chassis";
            param.setUrl(chassisUrl);
            String chassisResponse = sendHPILORequest(param);
            log.info("Chassis Info::" + chassisResponse);

            // 获取服务器信息
            String systemsUrl = baseUrl + "/redfish/v1/Systems";
            param.setUrl(systemsUrl);
            String systemsResponse = sendHPILORequest(param);
            log.info("systemsResponse Info::" + systemsResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Reply.ok("获取数据成功");
    }

    private void conncetTextIOL(ILOInstanceParam param) throws IOException {
        // 创建HttpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 设置iLO地址和认证信息
        String baseUrl = "https://" + param.getIPAdress();
        // 获取刀箱信息
        String iloUrl = baseUrl + "/redfish/v1";
        String iloUsername = param.getUserName();
        String iloPassword = param.getPassWord();
        log.info("conncetTextIOL::param"+param);
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
        log.info("responseString::"+responseString);
    }


    /**
     * 发送HTTP请求
     */
    private String sendHPILORequest(ILOInstanceParam param) {
        String responseString = "";
        try {
            //查询代理服务器
            List<ProxyInfo> proxyInfos = new ArrayList<>();
            responseString = proxySearch.doProxySearch(String.class, proxyInfos, param.getModelInstanceId()
                    , "mwHPChassisService", "sendHPILORequest", param, null);
            if (proxyInfos.size() > 0) {
                return responseString;
            }
            String authentication = param.getUserName() + ":" + param.getPassWord();
            String authorization = "Basic " + Base64.getEncoder().encodeToString(authentication.getBytes());

            URL url = new URL(param.getUrl());
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
                responseString = response.toString();
            } else {
                log.info("Failed to retrieve data from iLO4. Response code: " + responseCode);
            }
            conn.disconnect();
        } catch (IOException e) {
            log.error("getAllILODataInfo::连接失败", e);
        }
        return responseString;
    }
}
