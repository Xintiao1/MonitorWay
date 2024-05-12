package cn.mw.monitor.agent.impl;

import feign.Client;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@Slf4j
public class NacosClient implements Client {

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        log.info("use NacosSearchAllInstancClient {}" ,request.url());

        CloseableHttpClient httpClient = HttpClients.createDefault();
        Response response = null;
        try {
            HttpPost httpPost = new HttpPost();
            httpPost.setURI(new URI(request.url()));
            StringEntity stringEntity = new StringEntity(new String(request.body()) , ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            byte[] responseBody = EntityUtils.toByteArray(httpResponse.getEntity());

            // http response -> feign response
            response = Response.builder()
                    .body(responseBody)
                    .status(httpResponse.getStatusLine().getStatusCode())
                    .headers(new HashMap<>())
                    .build();
        } catch (URISyntaxException e) {
            log.error("NacosSearchAllInstancClient error" ,e);
        }
        return response;
    }
}
