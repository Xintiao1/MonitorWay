package cn.mw.monitor.TPServer.service.impl;

import cn.mw.monitor.TPServer.model.TPServerTypeEnum;
import cn.mw.monitor.service.tpserver.api.MWTPServerProxyListener;
import cn.mw.monitor.service.tpserver.model.ProxyServerInfo;
import cn.mw.monitor.service.tpserver.model.TPResult;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.zbx.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
@Slf4j
public class MWTPServerProxyListenerImp implements MWTPServerProxyListener {

    @Value("${scheduling.taskurl}")
    private String taskUrl;

    @Value("${scheduling.hasTask}")
    private boolean hasTask;

    @Value("${monitor.zabbix.debug}")
    private boolean debug;

    private CloseableHttpClient httpClient;

    @PreDestroy
    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.error("close httpclient error!", e);
            }
            httpClient = null;
        }
    }

    @Override
    public TPResult refreshServers() {
        try {
            MWTPServerProxy.refresh();

            synchronized (this) {
                if (null == httpClient) {
                    httpClient = HttpClients.custom().build();
                }
            }

            if(hasTask) {

                HttpUriRequest httpRequest = RequestBuilder.post().setUri(taskUrl)
                        .addHeader("Content-Type", "application/json").build();

                CloseableHttpResponse response = httpClient.execute(httpRequest);
                HttpEntity entity = response.getEntity();
                JsonNode result = new ObjectMapper().readTree(entity.getContent());

                if (result.has("error")) {
                    return new TPResult(false, "task server update error!");
                } else {
                    log.info("refresh task success!");
                }
            }
        }catch (Exception e){
            log.error("MWTPServerProxyListenerImp", e);
            return new TPResult(false, "MWTPServerProxyListenerImp 异常!");
        }
        return new TPResult(true, "MWTPServerProxyListenerImp 更新成功!");
    }

    @Override
    public boolean check(ProxyServerInfo proxyServerInfo) {
        String monitoringServerVersion = proxyServerInfo.getVersion().replace(MWTPServerProxy.DATA_SEP, MWTPServerProxy.VERSION_SEP);
        String enumKey = proxyServerInfo.getServerType() + monitoringServerVersion;
        TPServerTypeEnum tpServerTypeEnum = TPServerTypeEnum.valueOf(enumKey);
        String privateKey = RSAUtils.RSA_PRIVATE_KEY;
        //用私钥解密后的密码
        String passWdReal = RSAUtils.decryptData(proxyServerInfo.getPasswd(),privateKey);
        boolean result = false;
        if(Strings.isNullOrEmpty(passWdReal)){
            return result;
        }
        switch (tpServerTypeEnum) {
            case Zabbix6_0:
                MWZabbixApiV6 mwZabbixApiV6 = new MWZabbixApiV6();
                mwZabbixApiV6.setZabbixUrl(proxyServerInfo.getUrl());
                mwZabbixApiV6.setZabbixUser(proxyServerInfo.getUser());
                mwZabbixApiV6.setZabbixPassword(passWdReal);
                mwZabbixApiV6.setDebug(debug);
                result = mwZabbixApiV6.init();
                break;
            case Zabbix5_0:
                MWZabbixApiV5 mwZabbixApiV5 = new MWZabbixApiV5();
                mwZabbixApiV5.setZabbixUrl(proxyServerInfo.getUrl());
                mwZabbixApiV5.setZabbixUser(proxyServerInfo.getUser());
                mwZabbixApiV5.setZabbixPassword(passWdReal);
                mwZabbixApiV5.setDebug(debug);
                result = mwZabbixApiV5.init();
                break;
            case Zabbix4_0:
                MWZabbixApiV4 mwZabbixApiV4 = new MWZabbixApiV4();
                mwZabbixApiV4.setZabbixUrl(proxyServerInfo.getUrl());
                mwZabbixApiV4.setZabbixUser(proxyServerInfo.getUser());
                mwZabbixApiV4.setZabbixPassword(passWdReal);
                mwZabbixApiV4.setDebug(debug);
                result = mwZabbixApiV4.init();
                break;
            default:
                log.error("MWTPServerProxy error server:" + proxyServerInfo.toString());
        }
        return result;
    }
}
