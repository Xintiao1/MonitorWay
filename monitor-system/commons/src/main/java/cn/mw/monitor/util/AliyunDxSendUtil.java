package cn.mw.monitor.util;

import cn.mw.monitor.service.alert.dto.AlertRuleTableCommons;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.HttpClientConfig;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

@Slf4j
public class AliyunDxSendUtil {

    public static String send(String signName, String templateCode, String phone, String content, String accessKeyId, String accessKeySecret, AlertRuleTableCommons alertRuleTable) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-qingdao", accessKeyId, accessKeySecret);
        HttpClientConfig httpClientConfig = HttpClientConfig.getDefault();
        if(alertRuleTable!=null && alertRuleTable.getProxyState() != null && alertRuleTable.getProxyState()){
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(alertRuleTable.getProxyIp(),Integer.parseInt(alertRuleTable.getProxyPort())),
                    new UsernamePasswordCredentials(alertRuleTable.getProxyAccount(),alertRuleTable.getProxyPassword()));
            httpClientConfig.setExtParam("apache.httpclient.builder", HttpClientBuilder.create()
                .setProxy(new HttpHost(alertRuleTable.getProxyIp(),Integer.parseInt(alertRuleTable.getProxyPort())))
                    .setDefaultCredentialsProvider(credentialsProvider));
        }
        httpClientConfig.setProtocolType(ProtocolType.HTTPS);
        httpClientConfig.setIgnoreSSLCerts(true);
        profile.setHttpClientConfig(httpClientConfig);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", content);
        String result = null;
        try {
            CommonResponse response = client.getCommonResponse(request);

            result = response.getData();
            log.info("AliyunDX response:" + response.getData());

        } catch (Exception e) {
            log.error("error perform send message AliyunDX:",e);
            result = e.getMessage();
        }
        return result;
    }

}
